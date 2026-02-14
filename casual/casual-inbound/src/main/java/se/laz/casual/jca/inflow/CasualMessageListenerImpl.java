/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
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
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.jca.CasualResourceAdapterException;
import se.laz.casual.jca.SpanId;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerFactory;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerNotFoundException;
import se.laz.casual.jca.inflow.work.CasualServiceCallWork;
import se.laz.casual.network.InboundDeactivatedContext;
import se.laz.casual.network.InboundTopologyUpdateContext;
import se.laz.casual.network.ProtocolMatcher;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectReplyMessage;
import se.laz.casual.network.protocol.messages.domain.Service;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitReplyMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareReplyMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackReplyMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    private static Logger log = Logger.getLogger(CasualMessageListenerImpl.class.getName());
    @Override
    public void domainConnectRequest(CasualNWMessage<CasualDomainConnectRequestMessage> message, Channel channel)
    {
        log.finest(() -> "domainConnectRequest(). " + PrettyPrinter.format(message.getCorrelationId(), message.getMessage().getExecution()) + message );
        log.info(()-> "domainConnectRequest(). client" + channel + " asking for protocol version(s)" + message.getMessage().getProtocols());
        log.info(()-> "domainConnectRequest(). supported protocols: " + ProtocolVersion.supportedVersions());
        Long matchedProtocolVersion = ProtocolMatcher.match(message.getMessage().getProtocols());
        log.info(() -> "domainConnectRequest(). matched protocol version: " + ProtocolVersion.unmarshall(matchedProtocolVersion));

        if(matchedProtocolVersion >= ProtocolVersion.VERSION_1_1.getVersion())
        {
            // should be notified when RA is deactivated
            InboundDeactivatedContext.add(channel);
        }
        if(matchedProtocolVersion >= ProtocolVersion.VERSION_1_2.getVersion())
        {
            // should be notified whenever a domain connects
            // we send to all previously connected clients and add the new one after as not to send
            // the notification to the new client connecting
            InboundTopologyUpdateContext.sendTopologyUpdate(message.getMessage().getExecution());
            InboundTopologyUpdateContext.add(channel);
        }
        String domainName = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_NAME );
        UUID domainId = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_ID ).getId();
        CasualDomainConnectReplyMessage reply = CasualDomainConnectReplyMessage.createBuilder()
                                                                               .withDomainId( domainId )
                                                                               .withDomainName( domainName )
                                                                               .withExecution( message.getMessage().getExecution() )
                                                                               .withProtocolVersion(matchedProtocolVersion)
                                                                               .build();
        CasualNWMessage<CasualDomainConnectReplyMessage> replyMessage = CasualNWMessageImpl.of( message.getCorrelationId(), reply );
        channel.writeAndFlush(replyMessage);
    }

    @Override
    public void domainDisconnectReply(CasualNWMessage<DomainDisconnectReplyMessage> message)
    {
        log.finest(() -> "domainDisconnectReply(). " + PrettyPrinter.format(message.getCorrelationId(), message.getMessage().getExecution()) + message );
    }

    @Override
    public void domainDiscoveryRequest(CasualNWMessage<CasualDomainDiscoveryRequestMessage> message, Channel channel, ProtocolVersion protocolVersion)
    {
        log.finest(() -> "domainDiscoveryRequest(). " + PrettyPrinter.format(message.getCorrelationId(), message.getMessage().getExecution()) + message);

        String domainName = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_NAME );
        UUID domainId = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_ID ).getId();
        CasualDomainDiscoveryReplyMessage reply = CasualDomainDiscoveryReplyMessage.of( message.getMessage().getExecution(), domainId, domainName, protocolVersion);

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
    public void serviceCallRequest(CasualNWMessage<CasualServiceCallRequestMessage> message, Channel channel, WorkManager workManager, CasualInboundTransactionRegistry inboundTransactionRegistry, ProtocolVersion protocolVersion)
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
        SpanId spanId = SpanId.of();
        CasualServiceCallWork work = new CasualServiceCallWork(message.getCorrelationId(), message.getMessage(), isTpNoReply, protocolVersion, spanId);

        try
        {
            if(!isTpNoReply && isServiceCallTransactional( xid ) )
            {
                inboundTransactionRegistry.add(channel.id(), XidKey.of(xid));
                workManager.scheduleWork(work, WorkManager.INDEFINITE, createTransactionContext(xid, message.getMessage().getTimeout()), new ServiceCallWorkListener(channel, message.getMessage(), spanId, protocolVersion));
            }
            else
            {
                workManager.scheduleWork(work, WorkManager.INDEFINITE, null, new ServiceCallWorkListener(channel, message.getMessage(), isTpNoReply, spanId, protocolVersion));
            }
        }
        catch (WorkException e)
        {
            if(!isTpNoReply && isServiceCallTransactional( xid ) )
            {
                inboundTransactionRegistry.remove(channel.id(), XidKey.of(xid));
            }
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
    public void prepareRequest(CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> message, Channel channel, XATerminator xaTerminator, CasualInboundTransactionRegistry inboundTransactionRegistry)
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
            if(status == XAReturnCode.XA_RDONLY.getId())
            {
                // XA_RDONLY, no commit/rollback will be called
                inboundTransactionRegistry.remove(channel.id(), XidKey.of(xid));
            }
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
    public void commitRequest(CasualNWMessage<CasualTransactionResourceCommitRequestMessage> message, Channel channel, XATerminator xaTerminator, CasualInboundTransactionRegistry inboundTransactionRegistry)
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
            inboundTransactionRegistry.remove(channel.id(), XidKey.of(xid));
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
    public void requestRollback(CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> message, Channel channel, XATerminator xaTerminator, CasualInboundTransactionRegistry inboundTransactionRegistry)
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
            inboundTransactionRegistry.remove(channel.id(), XidKey.of(xid));
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
