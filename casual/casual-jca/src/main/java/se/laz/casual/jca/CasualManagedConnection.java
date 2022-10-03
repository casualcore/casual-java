/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import se.laz.casual.config.ConfigurationService;
import se.laz.casual.config.Domain;
import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.event.ConnectionEventHandler;
import se.laz.casual.jca.pool.NetworkPoolHandler;
import se.laz.casual.network.outbound.NettyConnectionInformation;
import se.laz.casual.network.outbound.NettyNetworkConnection;
import se.laz.casual.network.outbound.NetworkListener;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.CommException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.work.WorkManager;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * CasualManagedConnection
 * The application server pools these objects
 * It contains one physical connection and is used to create connection handle objects and transaction resources.
 * These in turn knows their managed connection and can invoke network operations.
 *
 * @version $Revision: $
 */
public class CasualManagedConnection implements ManagedConnection, NetworkListener
{
    private static final Logger log = Logger.getLogger(CasualManagedConnection.class.getName());

    private PrintWriter logwriter;
    private final CasualManagedConnectionFactory mcf;
    private final ConnectionEventHandler connectionEventHandler;
    private final List<CasualConnectionImpl> connectionHandles;
    private NetworkConnection networkConnection;
    private final Object networkConnectionLock = new Object();
    private CasualXAResource xaResource;
    private int timeout;

    /**
     * Create a new managed connection with the provided factory and request information.
     *
     * @param mcf managed connection factory which created this objet.
     */
    public CasualManagedConnection(CasualManagedConnectionFactory mcf)
    {
        this.mcf = mcf;
        this.logwriter = null;
        this.connectionEventHandler = new ConnectionEventHandler();
        this.connectionHandles = Collections.synchronizedList(new ArrayList<>(1));
        xaResource = new CasualXAResource(this, mcf.getResourceId());
    }

    /**
     * Underlying physical network connection managed by this
     * managed connection instance.
     *
     * @return network connection.
     */
    public NetworkConnection getNetworkConnection()
    {
        synchronized (networkConnectionLock)
        {
            if (networkConnection == null)
            {
                if(null != mcf.getNetworkPoolName() && null == mcf.getNetworkPoolSize())
                {
                    log.warning(() -> "networkPoolName set to: " + mcf.getNetworkPoolName() + " but missing networkPoolSize!");
                }
                networkConnection = null != mcf.getNetworkPoolSize() && null != mcf.getNetworkPoolName() ? NetworkPoolHandler.getInstance().getOrCreate( mcf.getNetworkPoolName(), mcf.getAddress(), mcf.getCasualProtocolVersion(), this, mcf.getNetworkPoolSize()) : createOneToOneManagedConnection();
            }
        }
        return networkConnection;
    }

   @Override
    public Object getConnection(Subject subject,
                                ConnectionRequestInfo cxRequestInfo) throws ResourceException
    {
        try
        {
            log.finest("getConnection()");
            if (!getNetworkConnection().isActive())
            {
                closeNetworkConnection();
                throw new CommException("connection to casual is gone");
            }
            CasualConnectionImpl c = new CasualConnectionImpl(this);
            connectionHandles.add(c);
            return c;
        }
        catch (Exception e)
        {
            // Most likely what will be caught here is a Netty variant of java.net.ConnectException
            casualNotAvailable();

            throw new CommException(e);
        }
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException
    {
        log.finest("associateConnection()");
        Objects.requireNonNull( connection, "Null connection handle." );

        if (!(connection instanceof CasualConnectionImpl))
        {
            throw new ResourceException("Wrong type of connection handle.");
        }
        CasualConnectionImpl handle = (CasualConnectionImpl) connection;
        handle.getManagedConnection().removeHandle(handle);
        handle.setManagedConnection(this);
        addHandle(handle);
    }

    @Override
    public void cleanup() throws ResourceException
    {
        log.finest("cleanup()");
        for(CasualConnectionImpl c : connectionHandles)
        {
            c.invalidate();
        }
        connectionHandles.clear();
    }

    @Override
    public void destroy() throws ResourceException
    {
        log.finest(() -> "destroy()" + this);
        Optional<DomainId> domainId = Optional.ofNullable( null == networkConnection ? null : networkConnection.getDomainId());
        domainId.ifPresent(mcf::domainDisconnect);
        closeNetworkConnection();
        connectionHandles.clear();
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener)
    {
        log.finest("addConnectionEventListener()");
        connectionEventHandler.addConnectionEventListener( listener );
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        log.finest("removeConnectionEventListener()");
        connectionEventHandler.removeConnectionEventListener(listener);
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException
    {
        log.finest("getLogWriter()");
        return logwriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException
    {
        log.finest("setLogWriter()");
        logwriter = out;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException
    {
        log.finest("getLocalTransaction(), this is not supported.");
        throw new NotSupportedException( "LocalTransactions are not supported." );
    }

    @Override
    public synchronized XAResource getXAResource() throws ResourceException
    {
        log.finest("getXAResource()");
        return this.xaResource;
    }

    /**
     * Help method to provide the current transaction id.
     *
     * @return current Xid or null if no XA resource present.
     */
    public Xid getCurrentXid()
    {
        return xaResource.getCurrentXid();
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException
    {
        log.finest("getMetaData()");
        return new CasualManagedConnectionMetaData();
    }

    /**
     * Close handle
     *
     * @param handle The handle
     */
    public void closeHandle(CasualConnection handle)
    {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        event.setConnectionHandle(handle);
        connectionEventHandler.sendEvent(event);
    }

    private void closeNetworkConnection()
    {
        synchronized (networkConnectionLock)
        {
            if (null != networkConnection)
            {
                networkConnection.close();
                networkConnection = null;
            }
        }
    }

    private void addHandle(CasualConnectionImpl handle)
    {
        connectionHandles.add(handle);
    }

    private void removeHandle(CasualConnectionImpl handle)
    {
        connectionHandles.remove(handle);
    }

    public WorkManager getWorkManager()
    {
        ResourceAdapter ra = mcf.getResourceAdapter();
        if(ra instanceof CasualResourceAdapter)
        {
            return ((CasualResourceAdapter) ra).getWorkManager();
        }
        throw new CasualResourceAdapterException("resource adapter should be a casual resource adapter");
    }

    @Override
    public String toString()
    {
        return "CasualManagedConnection{" +
                ", xaResource=" + xaResource +
                '}';
    }

    public void casualNotAvailable()
    {
        // Mark transaction branch as read only when we fail to establish connection (i.e. casual is down)
        // If this isn't done the application server will attempt to commit this broken resource, which
        // does not make sense. This is imperative to enable retries for casual calls, because otherwise
        // the transaction is unusable.
        xaResource.setReadOnly();

        // Some application servers may reuse this resource after exception is thrown. As long as networkConnection
        // is null for the next use of this resource it should not be a problem.
        networkConnection = null;
    }

    @Override
    public void disconnected(Exception reason)
    {
        log.finest(() -> "disconnected: " + this);
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, reason);
        connectionEventHandler.sendEvent(event);
    }

    /**
     * The domain id for this connection
     * @return
     */
    public DomainId getDomainId()
    {
        return getNetworkConnection().getDomainId();
    }

    /**
     * The domain ids for the pool of which this managed connection is a member
     * @return
     */
    public List<DomainId> getPoolDomainIds()
    {
        return mcf.getPoolDomainIds();
    }

    public void setTransactionTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public int getTransactionTimeout()
    {
        return timeout;
    }

    private NetworkConnection createOneToOneManagedConnection()
    {
        Domain domain = ConfigurationService.getInstance().getConfiguration().getDomain();
        NettyConnectionInformation ci = NettyConnectionInformation.createBuilder().withAddress(new InetSocketAddress(mcf.getHostName(), mcf.getPortNumber()))
                                                                  .withProtocolVersion(mcf.getCasualProtocolVersion())
                                                                  .withDomainId(domain.getId())
                                                                  .withDomainName(domain.getName())
                                                                  .build();
        NetworkConnection newNetworkConnection = NettyNetworkConnection.of(ci, this);
        log.finest(() -> "created new nw connection " + this);
        return newNetworkConnection;
    }
}
