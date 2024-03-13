/*
 * Copyright (c) 2017 - 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import io.netty.channel.Channel;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.resource.NotSupportedException;
import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.work.TransactionContext;
import jakarta.resource.spi.work.WorkException;
import jakarta.resource.spi.work.WorkManager;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.XAFlags;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.api.util.PrettyPrinter;
import se.laz.casual.api.xa.XAReturnCode;
import se.laz.casual.api.xa.XID;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.config.Domain;
import se.laz.casual.jca.CasualResourceAdapterException;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerFactory;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerNotFoundException;
import se.laz.casual.jca.inflow.work.CasualServiceCallWork;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.domain.Service;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitReplyMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareReplyMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackReplyMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inbound Casual Message Listener, responsible for handling all inbound requests received.
 */
@MessageDriven(messageListenerInterface = CasualMessageListener.class,
        activationConfig =
                {
                        @ActivationConfigProperty(propertyName = "resourceAdapterJndiName", propertyValue = "eis/casualResouceAdapter"),

                })
public class CasualMessageListenerImpl implements CasualMessageListener
{
    private static final long MICROSECOND_FACTOR = 1000L;
    private static Logger log = Logger.getLogger(CasualMessageListenerImpl.class.getName());
    private static final Long CASUAL_PROTOCOL_VERSION = 1000L;

    @Override
    public void domainConnectRequest(CasualNWMessage<CasualDomainConnectRequestMessage> message, Channel channel)
    {
        log.finest(() -> "domainConnectRequest(). " + PrettyPrinter.format(message.getCorrelationId(), message.getMessage().getExecution()) + message );

        Domain domain = ConfigurationService.getInstance().getConfiguration().getDomain();
        CasualDomainConnectReplyMessage reply = CasualDomainConnectReplyMessage.createBuilder()
                                                                               .withDomainId( domain.getId() )
                                                                               .withDomainName( domain.getName() )
                                                                               .withExecution( message.getMessage().getExecution() )
                                                                               .withProtocolVersion(CASUAL_PROTOCOL_VERSION)
                                                                               .build();
        CasualNWMessage<CasualDomainConnectReplyMessage> replyMessage = CasualNWMessageImpl.of( message.getCorrelationId(), reply );
        channel.writeAndFlush(replyMessage);
    }


    @Override
    public void domainDiscoveryRequest(CasualNWMessage<CasualDomainDiscoveryRequestMessage> message, Channel channel)
    {
        log.finest(() -> "domainDiscoveryRequest(). " + PrettyPrinter.format(message.getCorrelationId(), message.getMessage().getExecution()) + message);

        Domain domain = ConfigurationService.getInstance().getConfiguration().getDomain();
        CasualDomainDiscoveryReplyMessage reply = CasualDomainDiscoveryReplyMessage.of( message.getMessage().getExecution(), domain.getId(), domain.getName() );

        List<Service> services = new ArrayList<>();

        for( String service: message.getMessage().getServiceNames() )
        {
            try
            {
                ServiceHandler handler = ServiceHandlerFactory.getHandler( service );
                ServiceInfo info = handler.getServiceInfo( service );

                services.add( Service.of( info.getServiceName(), info.getCategory(), info.getTransactionType()) );
            }
            catch( ServiceHandlerNotFoundException e )
            {
                //Service does not exist. Continue with the next one in the list.
            }
        }
        reply.setServices( services );

        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMessage = CasualNWMessageImpl.of( message.getCorrelationId(), reply );
        channel.writeAndFlush(replyMessage);
    }

    @Override
    public void serviceCallRequest(CasualNWMessage<CasualServiceCallRequestMessage> message, Channel channel, WorkManager workManager )
    {
        log.finest(() -> "serviceCallRequest(). " + PrettyPrinter.format(message.getCorrelationId(), message.getMessage().getExecution(), message.getMessage().getXid()) + message);

        Xid xid = message.getMessage().getXid();
        if(tpNoReplyOutOfProtocol( message, isServiceCallTransactional( xid )))
        {
            log.warning(() ->{
                String casualMessageInfo = String.format("xid: %s, correlation: %s, execution: %s",PrettyPrinter.casualStringify(message.getMessage().getXid()),
                        PrettyPrinter.casualStringify(message.getCorrelationId()), PrettyPrinter.casualStringify(message.getMessage().getExecution()));
                return "For message: " + message + " TPNOREPLY is set but the call is transactional. It is out of protocol so call will be issued but non transactional\n" + casualMessageInfo;
            });
        }
        boolean isTpNoReply = message.getMessage().getXatmiFlags().isSet(AtmiFlags.TPNOREPLY);
        CompletableFuture<Long> startupTimeFuture = new CompletableFuture<>();
        CasualServiceCallWork work = new CasualServiceCallWork(message.getCorrelationId(), message.getMessage() , isTpNoReply, startupTimeFuture);

        try
        {
            long startupInMilliseconds = !isTpNoReply && isServiceCallTransactional( xid ) ?
                    workManager.startWork( work, WorkManager.INDEFINITE, createTransactionContext( xid, message.getMessage().getTimeout() ), new ServiceCallWorkListener( channel ) ) :
                    workManager.startWork( work, WorkManager.INDEFINITE, null, (isTpNoReply ? null : new ServiceCallWorkListener( channel )));
            startupTimeFuture.complete(startupInMilliseconds * MICROSECOND_FACTOR);
            log.finest( ()->"Service call startup: "+ startupInMilliseconds + "ms.");
        }
        catch (WorkException e)
        {
            throw new CasualResourceAdapterException( "Error starting work.", e );
        }
    }

    private boolean tpNoReplyOutOfProtocol(CasualNWMessage<CasualServiceCallRequestMessage> message, boolean serviceCallTransactional)
    {
        Flag<AtmiFlags> flags = message.getMessage().getXatmiFlags();
        return flags.isSet(AtmiFlags.TPNOREPLY) && serviceCallTransactional;
    }

    private boolean isServiceCallTransactional( Xid xid )
    {
        return ! xid.equals( XID.NULL_XID);
    }

    private TransactionContext createTransactionContext( Xid xid, long timeout )
    {
        TransactionContext context = new TransactionContext();
        context.setXid(xid);
        Duration timeoutDuration = Duration.of(timeout, ChronoUnit.NANOS);
        if (timeoutDuration.getSeconds() > 0)
        {
            try
            {
                context.setTransactionTimeout(timeoutDuration.getSeconds());
            }
            catch (NotSupportedException e)
            {
                log.warning("Timeout is not set as is not supported. " + e.getMessage());
            }
        }
        return context;
    }

    @Override
    public void prepareRequest(CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> message, Channel channel, XATerminator xaTerminator)
    {
        log.finest(() ->  "prepareRequest(). " + PrettyPrinter.format(message.getCorrelationId(),
                message.getMessage().getExecution(), message.getMessage().getXid()) + "flags:" + message.getMessage().getFlags() + " " + message);

        Xid xid = message.getMessage().getXid();
        int status = -1;
        try
        {
            status = xaTerminator.prepare( xid );

        } catch (XAException e)
        {

            status = e.errorCode;
            log.log( Level.WARNING, e, ()-> "XAException prepare()" + e.getMessage() );
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
            CasualNWMessageImpl<CasualTransactionResourcePrepareReplyMessage> replyMessage = CasualNWMessageImpl.of(message.getCorrelationId(), reply);
            channel.writeAndFlush(replyMessage);
        }
    }

    @Override
    public void commitRequest(CasualNWMessage<CasualTransactionResourceCommitRequestMessage> message, Channel channel, XATerminator xaTerminator)
    {
        log.finest(() -> "commitRequest(). " + PrettyPrinter.format(message.getCorrelationId(), message.getMessage().getExecution(), message.getMessage().getXid()) + message);

        Xid xid = message.getMessage().getXid();
        boolean onePhase = message.getMessage().getFlags().isSet( XAFlags.TMONEPHASE );

        int status = -1;
        try
        {
            xaTerminator.commit( xid, onePhase );

        } catch (XAException e)
        {
            status = e.errorCode;
            log.log( Level.WARNING, e, ()-> "XAException commit()" + e.getMessage() );
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
            CasualNWMessageImpl<CasualTransactionResourceCommitReplyMessage> replyMessage = CasualNWMessageImpl.of( message.getCorrelationId(), reply );
            channel.writeAndFlush(replyMessage);
        }
    }

    @Override
    public void requestRollback(CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> message, Channel channel, XATerminator xaTerminator)
    {
        log.finest(() -> "requestRollback(). " + PrettyPrinter.format(message.getCorrelationId(), message.getMessage().getExecution(), message.getMessage().getXid()) + message );

        Xid xid = message.getMessage().getXid();

        int status = -1;
        try
        {
            xaTerminator.rollback( xid );

        } catch (XAException e)
        {
            status = e.errorCode;
            log.log( Level.WARNING, e, ()-> "XAException rollback()" + e.getMessage() );
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
            CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> replyMessage = CasualNWMessageImpl.of( message.getCorrelationId(), reply );
            channel.writeAndFlush(replyMessage);
        }
    }
}
