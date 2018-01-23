package se.kodarkatten.casual.jca.inflow;

import se.kodarkatten.casual.api.flags.XAFlags;
import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.jca.CasualResourceAdapterException;
import se.kodarkatten.casual.jca.inflow.work.CasualServiceCallWork;
import se.kodarkatten.casual.network.io.CasualNetworkReader;
import se.kodarkatten.casual.network.io.CasualNetworkWriter;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader;
import se.kodarkatten.casual.network.messages.domain.CasualDomainConnectReplyMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainConnectRequestMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.messages.domain.Service;
import se.kodarkatten.casual.network.messages.domain.TransactionType;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceCommitReplyMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourcePrepareReplyMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceRollbackReplyMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.resource.NotSupportedException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@MessageDriven(messageListenerInterface = CasualMessageListener.class,
        activationConfig =
                {
                        @ActivationConfigProperty(propertyName = "resourceAdapterJndiName", propertyValue = "eis/casualResouceAdapter"),

                })
public class CasualMessageListenerImpl implements CasualMessageListener
{
    private static Logger log = Logger.getLogger(CasualMessageListenerImpl.class.getName());

    private static final Long CASUAL_PROTOCOL_VERSION = 1000L;

    @Override
    public void domainConnectRequest(CasualNWMessageHeader header, SocketChannel channel)
    {
        log.info( "domainConnectRequest()." );
        CasualNWMessage<CasualDomainConnectRequestMessage> message = CasualNetworkReader.read( channel, header );


        CasualDomainConnectReplyMessage reply = CasualDomainConnectReplyMessage.createBuilder()
                .withDomainId( message.getMessage().getDomainId() )
                .withDomainName( message.getMessage().getDomainName() )
                .withExecution( message.getMessage().getExecution() )
                .withProtocolVersion(CASUAL_PROTOCOL_VERSION)
                .build();
        CasualNWMessage<CasualDomainConnectReplyMessage> replyMessage = CasualNWMessage.of( message.getCorrelationId(), reply );

        CasualNetworkWriter.write( channel, replyMessage );
    }

    @Override
    public void domainDiscoveryRequest(CasualNWMessageHeader header, SocketChannel channel)
    {
        log.info( "domainDiscoveryRequest()." );

        CasualNWMessage<CasualDomainDiscoveryRequestMessage> message = CasualNetworkReader.read( channel, header );


        CasualDomainDiscoveryReplyMessage reply = CasualDomainDiscoveryReplyMessage.of( message.getMessage().getExecution(), message.getMessage().getDomainId(), message.getMessage().getDomainName() );

        List<Service> services = new ArrayList<>();
        //TODO: lookup service for reply, now just say yes to everything.
        for( String service: message.getMessage().getServiceNames() )
        {
            services.add( Service.of( service, "", TransactionType.NONE ) );
        }
        reply.setServices( services );

        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMessage = CasualNWMessage.of( message.getCorrelationId(), reply );

        CasualNetworkWriter.write( channel, replyMessage );
    }

    @Override
    public void serviceCallRequest(CasualNWMessageHeader header, SocketChannel channel, WorkManager workManager )
    {
        log.info( "serviceCallRequest()." );

        CasualNWMessage<CasualServiceCallRequestMessage> message = CasualNetworkReader.read( channel, header );

        CasualServiceCallWork work = new CasualServiceCallWork(header, message.getMessage(), channel );

        Xid xid = message.getMessage().getXid();

        try
        {
            long startup = isServiceCallTransactional( xid ) ?
                    workManager.startWork( work, WorkManager.INDEFINITE, createTransactionContext( xid, message.getMessage().getTimeout() ), null ) :
                    workManager.startWork( work );
            log.finest( ()->"Service call startup: "+ startup + "ms.");
        }
        catch (WorkException e)
        {
            throw new CasualResourceAdapterException( "Error starting work.", e );
        }
    }

    private boolean isServiceCallTransactional( Xid xid )
    {
        return ! xid.equals( XID.NULL_XID);
    }

    private TransactionContext createTransactionContext( Xid xid, long timeout )
    {
        TransactionContext context = new TransactionContext();
        context.setXid(xid);

        if (timeout > 0)
        {
            try
            {
                context.setTransactionTimeout(timeout);
            }
            catch (NotSupportedException e)
            {
                log.warning("Timeout is not set as is not supported. " + e.getMessage());
            }
        }
        return context;
    }

    @Override
    public void prepareRequest(CasualNWMessageHeader header, SocketChannel channel, XATerminator xaTerminator)
    {
        log.info( "prepareRequest()." );

        CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> message = CasualNetworkReader.read( channel, header );
        Xid xid = message.getMessage().getXid();
        int status = -1;
        try
        {
            status = xaTerminator.prepare( xid );

        } catch (XAException e)
        {

            status = e.errorCode;
            log.warning( e.getMessage() );
        }
        finally
        {

            CasualTransactionResourcePrepareReplyMessage reply =
                    CasualTransactionResourcePrepareReplyMessage.of(
                            message.getMessage().getExecution(),
                            xid,
                            message.getMessage().getResourceId(),
                            XAReturnCode.unmarshal(status)
                    );
            CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> replyMessage = CasualNWMessage.of(message.getCorrelationId(), reply);
            CasualNetworkWriter.write(channel, replyMessage);
        }
    }

    @Override
    public void commitRequest(CasualNWMessageHeader header, SocketChannel channel, XATerminator xaTerminator)
    {
        log.info( "commitRequest()." );
        CasualNWMessage<CasualTransactionResourceCommitRequestMessage> message = CasualNetworkReader.read( channel, header );
        Xid xid = message.getMessage().getXid();
        boolean onePhase = message.getMessage().getFlags().isSet( XAFlags.TMONEPHASE );

        int status = -1;
        try
        {
            xaTerminator.commit( xid, onePhase );

        } catch (XAException e)
        {
            status = e.errorCode;
            log.warning( e.getMessage() );
        }
        finally
        {
            CasualTransactionResourceCommitReplyMessage reply =
                    CasualTransactionResourceCommitReplyMessage.of(
                            message.getMessage().getExecution(),
                            xid,
                            message.getMessage().getResourceId(),
                            status == -1 ? XAReturnCode.XA_OK : XAReturnCode.unmarshal( status )
                    );
            CasualNWMessage<CasualTransactionResourceCommitReplyMessage> replyMessage = CasualNWMessage.of( message.getCorrelationId(), reply );
            CasualNetworkWriter.write( channel, replyMessage );
        }
    }

    @Override
    public void requestRollback(CasualNWMessageHeader header, SocketChannel channel, XATerminator xaTerminator)
    {
        log.info( "requestRollback()." );
        CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> message = CasualNetworkReader.read( channel, header );
        Xid xid = message.getMessage().getXid();

        int status = -1;
        try
        {
            xaTerminator.rollback( xid );

        } catch (XAException e)
        {
            status = e.errorCode;
            log.warning( e.getMessage() );
        }
        finally
        {
            CasualTransactionResourceRollbackReplyMessage reply =
                    CasualTransactionResourceRollbackReplyMessage.of(
                            message.getMessage().getExecution(),
                            xid,
                            message.getMessage().getResourceId(),
                            status == -1 ? XAReturnCode.XA_OK : XAReturnCode.unmarshal( status )
                    );
            CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> replyMessage = CasualNWMessage.of( message.getCorrelationId(), reply );
            CasualNetworkWriter.write( channel, replyMessage );
        }
    }
}
