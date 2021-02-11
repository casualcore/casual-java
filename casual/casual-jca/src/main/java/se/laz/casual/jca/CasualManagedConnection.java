/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import se.laz.casual.api.xa.XID;
import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.event.ConnectionEventHandler;
import se.laz.casual.network.outbound.NetworkListener;
import se.laz.casual.network.outbound.NettyConnectionInformation;
import se.laz.casual.network.outbound.NettyNetworkConnection;

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
import java.util.UUID;
import java.util.logging.Logger;

/**
 * CasualManagedConnection
 * The application server pools these objects
 * It contains one physical connection and is used to create connection handle objects and transaction resources.
 * These in turn knows their managed connection and can invoke network operations.
 * @version $Revision: $
 */
public class CasualManagedConnection implements ManagedConnection, NetworkListener
{
    private static final String DOMAIN_NAME = "casual-java";
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
    }

    /**
     * Domain name of the current managed connection instance.
     *
     * @return name of the associated domain.
     */
    public String getDomainName()
    {
        return DOMAIN_NAME;
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
                NettyConnectionInformation ci = NettyConnectionInformation.createBuilder().withAddress(new InetSocketAddress(mcf.getHostName(), mcf.getPortNumber()))
                                                                          .withProtocolVersion(mcf.getCasualProtocolVersion())
                                                                          .withDomainId(UUID.randomUUID())
                                                                          .withDomainName(DOMAIN_NAME)
                                                                          .build();
                networkConnection = NettyNetworkConnection.of(ci, this);
                log.finest(()->"created new nw connection " + this);
            }
        }
        return networkConnection;
    }

    @Override
    public Object getConnection(Subject subject,
                                ConnectionRequestInfo cxRequestInfo) throws ResourceException
    {
        log.finest("getConnection()");
        if(!getNetworkConnection().isActive())
        {
            closeNetworkConnection();
            throw new CommException("connection to casual is gone");
        }
        CasualConnectionImpl c = new CasualConnectionImpl(this );
        connectionHandles.add(c);
        return c;
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
        if(null != xaResource)
        {
            xaResource.reset();
        }
    }

    @Override
    public void destroy() throws ResourceException
    {
        log.finest(() -> "destroy()" + this);
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
        if( this.xaResource == null )
        {
            this.xaResource = new CasualXAResource(this, mcf.getResourceId());
        }
        return this.xaResource;
    }

    /**
     * Help method to provide the current transaction id.
     *
     * @return current Xid or null if no XA resource present.
     */
    public Xid getCurrentXid()
    {
        return (xaResource) == null ? XID.NULL_XID : xaResource.getCurrentXid();
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

    @Override
    public void disconnected()
    {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED);
        connectionEventHandler.sendEvent(event);
    }

    public void setTransactionTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public int getTransactionTimeout()
    {
        return timeout;
    }
}
