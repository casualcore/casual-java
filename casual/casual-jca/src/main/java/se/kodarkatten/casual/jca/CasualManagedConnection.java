package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.api.CasualServiceApi;
import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.ServiceReturn;
import se.kodarkatten.casual.api.flags.*;
import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.internal.buffer.CasualBufferBase;
import se.kodarkatten.casual.network.connection.CasualConnectionException;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;
import se.kodarkatten.casual.network.messages.transaction.*;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;

import javax.security.auth.Subject;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * CasualManagedConnection
 * The application server pools these objects
 * It contains one physical connection and is used to create handle objects
 * These in turn knows their managed connection and can invoke network operations through its managed connection
 * @version $Revision: $
 */
public class CasualManagedConnection implements ManagedConnection, CasualServiceApi
{
    private static final String DOMAIN_NAME = "casual-java";
    /**
     * The logger
     */
    private static final Logger log = Logger.getLogger(CasualManagedConnection.class.getName());

    /**
     * The logwriter
     */
    private PrintWriter logwriter;

    /**
     * ManagedConnectionFactory
     */
    private final CasualManagedConnectionFactory mcf;

    /**
     * Listeners
     */
    private final List<ConnectionEventListener> listeners;

    /**
     * Connections
     */
    private final List<CasualConnectionImpl> connectionHandles;


    private final NetworkConnection networkConnection;

    private Xid currentXid;

    /**
     * Default constructor
     *
     * @param mcf mcf
     * @param cxRequestInfo
     */
    public CasualManagedConnection(CasualManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo)
    {
        this.mcf = mcf;
        this.logwriter = null;
        this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
        this.connectionHandles = Collections.synchronizedList(new ArrayList<CasualConnectionImpl>(1));
        this.networkConnection = CasualNetworkConnection.of(new InetSocketAddress(mcf.getHostName(), mcf.getPortNumber()));
    }

    /**
     * Creates a new connection handle for the underlying physical connection
     * represented by the ManagedConnection instance.
     *
     * @param subject       Security context as JAAS subject
     * @param cxRequestInfo ConnectionRequestInfo instance
     * @return generic Object instance representing the connection handle.
     * @throws ResourceException generic exception if operation fails
     */
    @Override
    public Object getConnection(Subject subject,
                                ConnectionRequestInfo cxRequestInfo) throws ResourceException
    {
        log.finest("getConnection()");
        CasualConnectionImpl c = new CasualConnectionImpl(this, mcf);
        connectionHandles.add(c);
        return c;
    }

    /**
     * Used by the container to change the association of an
     * application-level connection handle with a ManagedConneciton instance.
     *
     * The handle also is set as the currently active connection
     * @param connection Application-level connection handle
     * @throws ResourceException generic exception if operation fails
     */
    @Override
    public void associateConnection(Object connection) throws ResourceException
    {
        log.finest("associateConnection()");
        if (connection == null)
        {
            throw new ResourceException("Null connection handle");
        }

        if (!(connection instanceof CasualConnectionImpl))
        {
            throw new ResourceException("Wrong type of connection handle");
        }
        CasualConnectionImpl handle = (CasualConnectionImpl) connection;
        handle.getManagedConnection().removeHandle(handle);
        handle.setManagedConnection(this);
        addHandle(handle);
    }

    /**
     * Application server calls this method to force any cleanup on the ManagedConnection instance.
     *
     * @throws ResourceException generic exception if operation fails
     */
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

    /**
     * Destroys the physical connections to the underlying resource manager.
     *
     * @throws ResourceException generic exception if operation fails
     */
    @Override
    public void destroy() throws ResourceException
    {
        log.finest("destroy()");
        networkConnection.close();
        connectionHandles.clear();
    }

    /**
     * Adds a connection event listener to the ManagedConnection instance.
     *
     * @param listener A new ConnectionEventListener to be registered
     */
    @Override
    public void addConnectionEventListener(ConnectionEventListener listener)
    {
        log.finest("addConnectionEventListener()");
        if (listener == null)
        {
            throw new IllegalArgumentException("Listener is null");
        }
        listeners.add(listener);
    }

    /**
     * Removes an already registered connection event listener from the ManagedConnection instance.
     *
     * @param listener already registered connection event listener to be removed
     */
    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener)
    {
        log.finest("removeConnectionEventListener()");
        if (listener == null)
        {
            throw new IllegalArgumentException("Listener is null");
        }
        listeners.remove(listener);
    }

    /**
     * Gets the log writer for this ManagedConnection instance.
     *
     * @return Character output stream associated with this Managed-Connection instance
     * @throws ResourceException generic exception if operation fails
     */
    @Override
    public PrintWriter getLogWriter() throws ResourceException
    {
        log.finest("getLogWriter()");
        return logwriter;
    }

    /**
     * Sets the log writer for this ManagedConnection instance.
     *
     * @param out Character Output stream to be associated
     * @throws ResourceException generic exception if operation fails
     */
    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException
    {
        log.finest("setLogWriter()");
        logwriter = out;
    }

    /**
     * Returns an <code>javax.resource.spi.LocalTransaction</code> instance.
     *
     * @return LocalTransaction instance
     * @throws ResourceException generic exception if operation fails
     */
    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException
    {
        log.finest("getLocalTransaction()");
        return null;
    }

    /**
     * Returns an <code>javax.transaction.xa.XAresource</code> instance.
     *
     * @return XAResource instance
     * @throws ResourceException generic exception if operation fails
     */
    @Override
    public XAResource getXAResource() throws ResourceException
    {
        log.finest("getXAResource()");
        return new CasualXAResource(this);
    }

    /**
     * Gets the metadata information for this connection's underlying EIS resource manager instance.
     *
     * @return ManagedConnectionMetaData instance
     * @throws ResourceException generic exception if operation fails
     */
    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException
    {
        log.finest("getMetaData()");
        return new CasualManagedConnectionMetaData();
    }

    public void start(Xid xid, int i) throws XAException
    {
        if(!(XAFlags.TMJOIN.getValue() == i || XAFlags.TMRESUME.getValue() == i))
        {
            if(CasualTransactionResources.getInstance().xidPending(xid))
            {
                throw new XAException(XAException.XAER_DUPID);
            }
        }
        currentXid = xid;
    }

    public final int prepareRequest(Xid xid) throws XAException
    {
        Flag<XAFlags> flags = Flag.of(XAFlags.TMNOFLAGS);
        Long resourceId = CasualTransactionResources.getInstance().getResourceIdForXid(xid);
        CasualTransactionResourcePrepareRequestMessage prepareRequest = CasualTransactionResourcePrepareRequestMessage.of(UUID.randomUUID(),xid,resourceId,flags);
        CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> requestEnvelope = CasualNWMessage.of(UUID.randomUUID(), prepareRequest);
        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> replyEnvelope = networkConnection.requestReply(requestEnvelope);
        CasualTransactionResourcePrepareReplyMessage replyMsg = replyEnvelope.getMessage();

        if(notOkTransactionReturnCodeForPrepare(replyMsg.getTransactionReturnCode()))
        {
            throw new XAException(replyMsg.getTransactionReturnCode().getId());
        }
        return replyMsg.getTransactionReturnCode().getId();
    }

    public final void commitRequest(Xid xid, boolean onePhaseCommit) throws XAException
    {
        Flag<XAFlags> flags = Flag.of(XAFlags.TMNOFLAGS);
        if (onePhaseCommit)
        {
            flags = Flag.of(XAFlags.TMONEPHASE);
        }
        Long resourceId = CasualTransactionResources.getInstance().getResourceIdForXid(xid);
        CasualTransactionResourceCommitRequestMessage commitRequest =
                CasualTransactionResourceCommitRequestMessage.of(UUID.randomUUID(), xid, resourceId, flags);
        CasualTransactionResources.getInstance().removeResourceIdForXid(xid);
        CasualNWMessage<CasualTransactionResourceCommitRequestMessage> requestEnvelope = CasualNWMessage.of(UUID.randomUUID(), commitRequest);
        CasualNWMessage<CasualTransactionResourceCommitReplyMessage> replyEnvelope = networkConnection.requestReply(requestEnvelope);
        CasualTransactionResourceCommitReplyMessage replyMsg = replyEnvelope.getMessage();
        if(!(replyMsg.getTransactionReturnCode() == XAReturnCode.XA_OK || replyMsg.getTransactionReturnCode() == XAReturnCode.XA_RDONLY))
        {
            throw new XAException(replyMsg.getTransactionReturnCode().getId());
        }
    }

    public void rollbackRequest(final Xid xid) throws XAException
    {
        Flag<XAFlags> flags = Flag.of(XAFlags.TMNOFLAGS);
        Long resourceId = CasualTransactionResources.getInstance().getResourceIdForXid(xid);
        CasualTransactionResourceRollbackRequestMessage request =
            CasualTransactionResourceRollbackRequestMessage.of(UUID.randomUUID(), xid, resourceId, flags);
        CasualTransactionResources.getInstance().removeResourceIdForXid(xid);
        CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> requestEnvelope = CasualNWMessage.of(UUID.randomUUID(), request);
        CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> replyEnvelope = networkConnection.requestReply(requestEnvelope);
        CasualTransactionResourceRollbackReplyMessage replyMsg = replyEnvelope.getMessage();
        if(!(replyMsg.getTransactionReturnCode() == XAReturnCode.XA_OK || replyMsg.getTransactionReturnCode() == XAReturnCode.XA_RDONLY))
        {
            throw new XAException(replyMsg.getTransactionReturnCode().getId());
        }
    }

    // CasualServiceApi
    // TODO:
    // maybe move these somewhere else
    // regardless, they should all use the single physical connection from the managed connection
    @Override
    public <X extends CasualBuffer> ServiceReturn<X> tpcall(String serviceName, X data, Flag<AtmiFlags> flags, Class<X> bufferClass)
    {
        final UUID corrid = UUID.randomUUID();
        if(serviceExists(corrid, serviceName))
        {
            return makeServiceCall(corrid, serviceName, data, flags, bufferClass);
        }
        throw new CasualConnectionException("service " + serviceName + " does not exist");
    }

    @Override
    public <X extends CasualBuffer> CompletableFuture<ServiceReturn<X>> tpacall(String serviceName, X data, Flag<AtmiFlags> flags, Class<X> bufferClass)
    {
        throw new CasualConnectionException("not yet implemented");
    }

    private <X extends CasualBuffer> ServiceReturn<X> makeServiceCall(UUID corrid, String serviceName, X data, Flag<AtmiFlags> flags, Class<X> bufferClass)
    {
        CasualServiceCallRequestMessage serviceRequestMessage = CasualServiceCallRequestMessage.createBuilder()
                                                                                               .setExecution(UUID.randomUUID())
                                                                                               .setServiceBuffer(ServiceBuffer.of(data.getType(), data.getBytes()))
                                                                                               .setServiceName(serviceName)
                                                                                               .setXid(currentXid)
                                                                                               .setXatmiFlags(flags).build();

        CasualNWMessage<CasualServiceCallRequestMessage> serviceRequestNetworkMessage = CasualNWMessage.of(corrid, serviceRequestMessage);
        CasualNWMessage<CasualServiceCallReplyMessage> serviceReplyNetworkMessage = networkConnection.requestReply(serviceRequestNetworkMessage);
        CasualServiceCallReplyMessage serviceReplyMessage = serviceReplyNetworkMessage.getMessage();
        CasualBufferBase<X> buffer = CasualBufferBase.of(serviceReplyMessage.getServiceBuffer(), bufferClass);
        return new ServiceReturn<>(bufferClass.cast(buffer), (serviceReplyMessage.getError() == ErrorState.OK) ? ServiceReturnState.TPSUCCESS : ServiceReturnState.TPFAIL, serviceReplyMessage.getError());
    }

    private boolean serviceExists(UUID corrid, String serviceName)
    {
        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                                            .setExecution(UUID.randomUUID())
                                                                                            .setDomainId(UUID.randomUUID())
                                                                                            .setDomainName(DOMAIN_NAME)
                                                                                            .setServiceNames(Arrays.asList(serviceName))
                                                                                            .build();
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> msg = CasualNWMessage.of(corrid, requestMsg);
        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMsg = networkConnection.requestReply(msg);
        return replyMsg.getMessage().getServices().stream()
                       .map(s -> s.getName())
                       .filter(v -> v.equals(serviceName))
                       .findFirst()
                       .map( v -> true)
                       .orElse(false);
    }


    /**
     * Close handle
     *
     * @param handle The handle
     */
    void closeHandle(CasualConnection handle)
    {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        event.setConnectionHandle(handle);
        sendEvent(event);
    }

    private boolean notOkTransactionReturnCodeForPrepare(final XAReturnCode transactionReturnCode)
    {
        return !(transactionReturnCode == XAReturnCode.XA_OK || transactionReturnCode == XAReturnCode.XA_RDONLY);
    }

    private void addHandle(CasualConnectionImpl handle)
    {
        connectionHandles.add(handle);
    }

    private void removeHandle(CasualConnectionImpl handle)
    {
        connectionHandles.remove(handle);
    }

    private void sendEvent(ConnectionEvent event)
    {
        synchronized (listeners)
        {
            for (ConnectionEventListener l : listeners)
            {
                switch (event.getId())
                {
                    case ConnectionEvent.CONNECTION_CLOSED:
                        l.connectionClosed(event);
                        break;
                    case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
                        l.connectionErrorOccurred(event);
                        break;
                    case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
                        l.localTransactionCommitted(event);
                        break;
                    case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
                        l.localTransactionRolledback(event);
                        break;
                    case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
                        l.localTransactionStarted(event);
                        break;
                    default:
                        // TODO:
                        // maybe not throw, just ignore?
                        throw new CasualConnectionException("unkown event:" + event);
                }
            }
        }
    }
}
