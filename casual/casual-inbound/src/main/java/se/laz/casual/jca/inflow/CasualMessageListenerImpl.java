/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import io.netty.channel.Channel;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.XAFlags;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.api.xa.XAReturnCode;
import se.laz.casual.api.xa.XID;
import se.laz.casual.jca.CasualResourceAdapterException;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerFactory;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerNotFoundException;
import se.laz.casual.jca.inflow.work.CasualServiceCallWork;
import se.laz.casual.network.grpc.MessageCreator;
import se.laz.casual.network.messages.CasualCommitReply;
import se.laz.casual.network.messages.CasualCommitRequest;
import se.laz.casual.network.messages.CasualDomainConnectReply;
import se.laz.casual.network.messages.CasualDomainConnectRequest;
import se.laz.casual.network.messages.CasualDomainDiscoveryReply;
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest;
import se.laz.casual.network.messages.CasualPrepareReply;
import se.laz.casual.network.messages.CasualPrepareRequest;
import se.laz.casual.network.messages.CasualReply;
import se.laz.casual.network.messages.CasualRequest;
import se.laz.casual.network.messages.CasualRollbackReply;
import se.laz.casual.network.messages.CasualRollbackRequest;
import se.laz.casual.network.messages.CasualServiceCallRequest;
import se.laz.casual.network.protocol.messages.domain.Service;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.resource.NotSupportedException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    private static final Long CASUAL_PROTOCOL_VERSION = 1000L;

    @Override
    public void domainConnectRequest(CasualRequest message, Channel channel)
    {
        log.finest( "domainConnectRequest()." );
        CasualReply.Builder envelope = CasualReply.newBuilder()
                                                  .setCorrelationId(message.getCorrelationId())
                                                  .setMessageType(CasualReply.MessageType.DOMAIN_CONNECT_REPLY);
        CasualDomainConnectRequest request = message.getDomainConnect();
        CasualDomainConnectReply reply = CasualDomainConnectReply.newBuilder()
                                                                 .setDomainId(request.getDomainId())
                                                                 .setDomainName(request.getDomainName())
                                                                 .setExecution(request.getExecution())
                                                                 .setProtocolVersion(CASUAL_PROTOCOL_VERSION)
                                                                 .build();
        channel.writeAndFlush(envelope.setDomainConnect(reply).build());
    }

    @Override
    public void domainDiscoveryRequest(CasualRequest message, Channel channel)
    {
        log.finest( "domainDiscoveryRequest()." );

        CasualReply.Builder envelope = CasualReply.newBuilder()
                                                  .setCorrelationId(message.getCorrelationId())
                                                  .setMessageType(CasualReply.MessageType.DOMAIN_DISCOVERY_REPLY);
        CasualDomainDiscoveryRequest request = message.getDomainDiscovery();

        CasualDomainDiscoveryReply.Builder replyBuilder = CasualDomainDiscoveryReply.newBuilder()
                                                                                    .setExecution(request.getExecution())
                                                                                    .setDomainId(request.getDomainId())
                                                                                    .setDomainName(request.getDomainName());
        List<Service> services = new ArrayList<>();

        for( String service: request.getServiceNamesList() )
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
        replyBuilder.addAllServices(services.stream()
                                            .map(s -> se.laz.casual.network.messages.Service.newBuilder()
                                                                                            .setTransactionType(MessageCreator.toTransactionType(s.getTransactionType()))
                                                                                            .setTimeout(s.getTimeout())
                                                                                            .setCategory(s.getCategory())
                                                                                            .setHops(s.getHops())
                                                                                            .setName(s.getName())
                                                                                            .build())
                                            .collect(Collectors.toList()));
        channel.writeAndFlush(envelope.setDomainDiscovery(replyBuilder.build()).build());
    }

    @Override
    public void serviceCallRequest(CasualRequest message, Channel channel, WorkManager workManager )
    {
        log.finest( "serviceCallRequest()." );

        CasualServiceCallRequest request = message.getServiceCall();
        CasualServiceCallWork work = new CasualServiceCallWork(MessageCreator.toUUID(message.getCorrelationId()), request );

        Xid xid = MessageCreator.toXID(request.getXid());

        try
        {
            long startup = isServiceCallTransactional( xid ) ?
                    workManager.startWork( work, WorkManager.INDEFINITE, createTransactionContext( xid, request.getTimeout() ), new ServiceCallWorkListener( channel ) ) :
                    workManager.startWork( work, WorkManager.INDEFINITE, null, new ServiceCallWorkListener( channel ));
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
    public void prepareRequest(CasualRequest message, Channel channel, XATerminator xaTerminator)
    {
        log.finest( "prepareRequest()." );

        CasualPrepareRequest request = message.getPrepare();

        Xid xid = MessageCreator.toXID(request.getXid());
        int status = -1;
        try
        {
            status = xaTerminator.prepare( xid );

        }
        catch (XAException e)
        {

            status = e.errorCode;
            log.log( Level.WARNING, e, ()-> "XAExcception prepare()" + e.getMessage() );
        }
        finally
        {
            CasualPrepareReply reply = CasualPrepareReply.newBuilder()
                                                         .setExecution(request.getExecution())
                                                         .setXid(request.getXid())
                                                         .setResourceManagerId(request.getResourceManagerId())
                                                         .setXaReturnCode(se.laz.casual.network.messages.XAReturnCode.valueOf(XAReturnCode.unmarshal(status).name()))
                                                         .build();

            CasualReply envelope = CasualReply.newBuilder()
                                                      .setCorrelationId(message.getCorrelationId())
                                                      .setMessageType(CasualReply.MessageType.PREPARE_REPLY)
                                                      .setPrepare(reply)
                                                      .build();
            channel.writeAndFlush(envelope);
        }
    }

    @Override
    public void commitRequest(CasualRequest message, Channel channel, XATerminator xaTerminator)
    {
        log.finest( "commitRequest()." );

        CasualCommitRequest request = message.getCommit();

        Xid xid = MessageCreator.toXID(request.getXid());

        boolean onePhase = new Flag.Builder<XAFlags>((int)request.getXaFlags()).build().isSet(XAFlags.TMONEPHASE);

        int status = -1;
        try
        {
            xaTerminator.commit( xid, onePhase );
        }
        catch (XAException e)
        {
            status = e.errorCode;
            log.log( Level.WARNING, e, ()-> "XAExcception commit()" + e.getMessage() );
        }
        finally
        {
            se.laz.casual.network.messages.XAReturnCode xaReturnCode = se.laz.casual.network.messages.XAReturnCode.valueOf(status == -1 ? XAReturnCode.XA_OK.name() : XAReturnCode.unmarshal( status ).name());
            CasualCommitReply reply = CasualCommitReply.newBuilder()
                                                       .setExecution(request.getExecution())
                                                       .setXid(request.getXid())
                                                       .setResourceManagerId(request.getResourceManagerId())
                                                       .setXaReturnCode(xaReturnCode)
                                                       .build();
            CasualReply envelope = CasualReply.newBuilder()
                                              .setCorrelationId(message.getCorrelationId())
                                              .setMessageType(CasualReply.MessageType.COMMIT_REPLY)
                                              .setCommit(reply)
                                              .build();
            channel.writeAndFlush(envelope);
        }
    }

    @Override
    public void requestRollback(CasualRequest message, Channel channel, XATerminator xaTerminator)
    {
        log.finest( "requestRollback()." );

        CasualRollbackRequest request = message.getRollback();

        Xid xid = MessageCreator.toXID(request.getXid());

        int status = -1;
        try
        {
            xaTerminator.rollback( xid );

        }
        catch (XAException e)
        {
            status = e.errorCode;
            log.log( Level.WARNING, e, ()-> "XAException rollback()" + e.getMessage() );
        }
        finally
        {
            se.laz.casual.network.messages.XAReturnCode xaReturnCode = se.laz.casual.network.messages.XAReturnCode.valueOf(status == -1 ? XAReturnCode.XA_OK.name() : XAReturnCode.unmarshal( status ).name());
            CasualRollbackReply reply = CasualRollbackReply.newBuilder()
                                                           .setExecution(request.getExecution())
                                                           .setXid(request.getXid())
                                                           .setResourceManagerId(request.getResourceManagerId())
                                                           .setXaReturnCode(xaReturnCode)
                                                           .build();
            CasualReply envelope = CasualReply.newBuilder()
                                              .setCorrelationId(message.getCorrelationId())
                                              .setMessageType(CasualReply.MessageType.ROLLBACK_REPLY)
                                              .setRollback(reply)
                                              .build();
            channel.writeAndFlush(envelope);
        }
    }
}
