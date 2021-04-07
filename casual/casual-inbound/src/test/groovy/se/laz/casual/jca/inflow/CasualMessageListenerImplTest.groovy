/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow

import com.google.protobuf.ByteString
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.flags.XAFlags
import se.laz.casual.api.service.CasualService
import se.laz.casual.api.xa.XAReturnCode
import se.laz.casual.api.xa.XID
import se.laz.casual.jca.CasualResourceAdapterException
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceMetaData
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceRegistry
import se.laz.casual.jca.inflow.work.CasualServiceCallWork
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualCommitReply
import se.laz.casual.network.messages.CasualCommitRequest
import se.laz.casual.network.messages.CasualDomainConnectReply
import se.laz.casual.network.messages.CasualDomainConnectRequest
import se.laz.casual.network.messages.CasualDomainDiscoveryReply
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest
import se.laz.casual.network.messages.CasualPrepareReply
import se.laz.casual.network.messages.CasualPrepareRequest
import se.laz.casual.network.messages.CasualReply
import se.laz.casual.network.messages.CasualRequest
import se.laz.casual.network.messages.CasualRollbackReply
import se.laz.casual.network.messages.CasualRollbackRequest
import se.laz.casual.network.messages.CasualServiceCallRequest
import se.laz.casual.network.messages.domain.TransactionType
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.XATerminator
import javax.resource.spi.work.ExecutionContext
import javax.resource.spi.work.WorkException
import javax.resource.spi.work.WorkListener
import javax.resource.spi.work.WorkManager
import javax.transaction.xa.XAException
import javax.transaction.xa.Xid
import java.lang.annotation.Annotation
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors

class CasualMessageListenerImplTest extends Specification
{
    @Shared CasualMessageListener instance
    @Shared EmbeddedChannel channel
    @Shared WorkManager workManager
    @Shared XATerminator xaTerminator

    @Shared UUID correlationId = UUID.randomUUID()
    @Shared UUID domainId = UUID.randomUUID()
    @Shared String domainName = "java"
    @Shared UUID execution = UUID.randomUUID()
    @Shared Xid xid
    @Shared String serviceName = "echo"
    @Shared TestInboundHandler inboundHandler

    def setup()
    {
        instance = new CasualMessageListenerImpl()
        inboundHandler = TestInboundHandler.of()
        channel = new EmbeddedChannel(new ProtobufVarint32FrameDecoder(), new ProtobufDecoder(CasualReply.getDefaultInstance()),
                new ProtobufVarint32LengthFieldPrepender(), new ProtobufEncoder(), inboundHandler)
        workManager = Mock( WorkManager )
        xaTerminator = Mock( XATerminator )

        xid = createXid()
    }

    Xid createXid()
    {
        String gid = Integer.toString( ThreadLocalRandom.current().nextInt() )
        String b = Integer.toString( ThreadLocalRandom.current().nextInt() )
        return XID.of(gid.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8), 0)
    }

    def "DomainConnectRequest"()
    {
        given:
        long protocolVersion = 1000L

        CasualDomainConnectRequest request = CasualDomainConnectRequest.newBuilder()
                .setDomainId(MessageCreator.toUUID4(domainId))
                .setDomainName(domainName)
                .setExecution(MessageCreator.toUUID4(execution))
                .addProtocolVersion(protocolVersion)
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.DOMAIN_CONNECT_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setDomainConnect(request)
                .build()
        when:
        instance.domainConnectRequest( message, channel )
        channel.writeInbound(channel.outboundMessages().element())
        CasualReply replyEnvelope = inboundHandler.getMsg()
        CasualDomainConnectReply reply = replyEnvelope.getDomainConnect()

        then:
        reply != null
        replyEnvelope.getMessageType() ==  CasualReply.MessageType.DOMAIN_CONNECT_REPLY
        MessageCreator.toUUID(replyEnvelope.getCorrelationId()) == correlationId
        reply.getDomainName() == domainName
        MessageCreator.toUUID(reply.getDomainId()) == domainId
        MessageCreator.toUUID(reply.getExecution()) == execution
        reply.getProtocolVersion() == protocolVersion
    }

    def "DomainDiscoveryRequest"()
    {
        given:
        CasualServiceMetaData metaData = CasualServiceMetaData.newBuilder()
                .service( new TestCasualService() )
                .serviceMethod( String.class.getMethod("toString"))
                .implementationClass( String.class )
                .build()
        CasualServiceRegistry.getInstance().register( metaData )

        List<String> serviceNames = Arrays.asList( serviceName )
        List<Service> expectedServices = Arrays.asList( Service.of( serviceName, "mycategory", TransactionType.AUTOMATIC ) )

        CasualDomainDiscoveryRequest request = CasualDomainDiscoveryRequest.newBuilder()
                .setDomainId(MessageCreator.toUUID4(domainId))
                .setDomainName(domainName)
                .setExecution(MessageCreator.toUUID4(execution))
                .addAllServiceNames(serviceNames)
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.DOMAIN_DISCOVERY_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setDomainDiscovery(request)
                .build()

        when:
        instance.domainDiscoveryRequest( message, channel )
        channel.writeInbound(channel.outboundMessages().element())
        CasualReply replyEnvelope = inboundHandler.getMsg()
        CasualDomainDiscoveryReply reply = replyEnvelope.getDomainDiscovery()

        then:
        reply != null
        replyEnvelope.getMessageType() == CasualReply.MessageType.DOMAIN_DISCOVERY_REPLY
        MessageCreator.toUUID(replyEnvelope.getCorrelationId()) == correlationId
        MessageCreator.toUUID(replyEnvelope.getCorrelationId()) == correlationId
        reply.getDomainName() == domainName
        MessageCreator.toUUID(reply.getDomainId()) == domainId
        MessageCreator.toUUID(reply.getExecution()) == execution
        toServiceList(reply.getServicesList()) == expectedServices
    }

    def "ServiceCallRequest"()
    {
        given:
        CasualServiceCallWork actualWork
        long actualStartTimeout
        ExecutionContext actualExecutionContext
        WorkListener actualWorkListener
        long timeout = 12L
        Duration timeoutDuration = Duration.of(timeout, ChronoUnit.SECONDS)
        String serviceName = "echo"

        byte[] payload = JsonBuffer.of( "{\"hello\"}").getBytes().get(0)

        CasualServiceCallRequest request = CasualServiceCallRequest.newBuilder()
                .setXid(MessageCreator.toXID(xid))
                .setExecution(MessageCreator.toUUID4(execution))
                .setServiceName(serviceName)
                .setBufferTypeName('json')
                .setPayload(ByteString.copyFrom(payload))
                .setTimeout(timeoutDuration.toNanos())
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.SERVICE_CALL_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setServiceCall(request)
                .build()

        when:
        instance.serviceCallRequest( message, channel, workManager )

        then:
        1 * workManager.startWork( _,_,_,_ ) >> {
            CasualServiceCallWork work, long startTimeout, ExecutionContext executionContext, WorkListener workListener ->
                actualWork = work
                actualStartTimeout = startTimeout
                actualExecutionContext = executionContext
                actualWorkListener = workListener
                return 1L
        }

        actualWork != null
        actualWork.getCorrelationId() == correlationId
        actualStartTimeout != null
        actualStartTimeout == WorkManager.INDEFINITE
        actualExecutionContext != null
        actualExecutionContext.getXid() == xid
        actualExecutionContext.getTransactionTimeout() == timeout
        actualWorkListener != null
    }

    def "ServiceCallRequest with null xid calls service work without transaction context."()
    {
        given:
        xid = XID.NULL_XID
        CasualServiceCallWork actualWork

        String serviceName = "echo"

        byte[] payload = JsonBuffer.of( "{\"hello\"}").getBytes().get(0)

        CasualServiceCallRequest request = CasualServiceCallRequest.newBuilder()
                .setXid(MessageCreator.toXID(xid))
                .setExecution(MessageCreator.toUUID4(execution))
                .setServiceName(serviceName)
                .setBufferTypeName('json')
                .setPayload(ByteString.copyFrom(payload))
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.SERVICE_CALL_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setServiceCall(request)
                .build()

        when:
        instance.serviceCallRequest( message, channel, workManager )

        then:
        1 * workManager.startWork(_, _, _, _) >> {
            CasualServiceCallWork work, long startTimeout, ExecutionContext executionContext, WorkListener workListener ->
                assert null == executionContext
                assert null != workListener
                actualWork = work
                return 1L
        }

        actualWork != null
        actualWork.getCorrelationId() == correlationId
    }

    def "ServiceCallRequest startWork throws exception, wrapped and thrown."()
    {
        given:
        String serviceName = "echo"

        byte[] payload = JsonBuffer.of( "{\"hello\"}").getBytes().get(0)
        CasualServiceCallRequest request = CasualServiceCallRequest.newBuilder()
                .setXid(MessageCreator.toXID(xid))
                .setExecution(MessageCreator.toUUID4(execution))
                .setServiceName(serviceName)
                .setBufferTypeName('json')
                .setPayload(ByteString.copyFrom(payload))
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.SERVICE_CALL_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setServiceCall(request)
                .build()

        when:
        instance.serviceCallRequest( message, channel, workManager )

        then:
        1 * workManager.startWork( _,_,_,_ ) >> {
            throw new WorkException( "Simulated error." )
        }

        thrown CasualResourceAdapterException
    }

    def "PrepareRequest"()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()

        CasualPrepareRequest request = CasualPrepareRequest.newBuilder()
                .setExecution(MessageCreator.toUUID4(execution))
                .setXid(MessageCreator.toXID(xid))
                .setResourceManagerId(resource)
                .setXaFlags(flag.getFlagValue())
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.PREPARE_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setPrepare(request)
                .build()

        when:
        instance.prepareRequest( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())
        CasualReply replyEnvelope = inboundHandler.getMsg()
        CasualPrepareReply reply = replyEnvelope.getPrepare()

        then:
        1 * xaTerminator.prepare( xid ) >> {
            return XAReturnCode.XA_OK.getId()
        }

        reply != null
        replyEnvelope.getMessageType() == CasualReply.MessageType.PREPARE_REPLY
        MessageCreator.toUUID(replyEnvelope.getCorrelationId()) == correlationId
        MessageCreator.toUUID(reply.getExecution()) == execution
        XAReturnCode.valueOf(reply.getXaReturnCode().name()) == XAReturnCode.XA_OK
    }

    def "PrepareRequest return code failure."()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()

        CasualPrepareRequest request = CasualPrepareRequest.newBuilder()
                .setExecution(MessageCreator.toUUID4(execution))
                .setXid(MessageCreator.toXID(xid))
                .setResourceManagerId(resource)
                .setXaFlags(flag.getFlagValue())
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.PREPARE_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setPrepare(request)
                .build()

        when:
        instance.prepareRequest( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())

        CasualReply replyEnvelope = inboundHandler.getMsg()
        CasualPrepareReply reply = replyEnvelope.getPrepare()

        then:
        1 * xaTerminator.prepare( xid ) >> {
            return XAReturnCode.XAER_RMERR.getId()
        }

        reply != null
        replyEnvelope.getMessageType() == CasualReply.MessageType.PREPARE_REPLY
        MessageCreator.toUUID(replyEnvelope.getCorrelationId()) == correlationId
        MessageCreator.toUUID(reply.getExecution()) == execution
        XAReturnCode.valueOf(reply.getXaReturnCode().name()) == XAReturnCode.XAER_RMERR
    }

    def "PrepareRequest prepare throws exception, returns exception error code."()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()

        CasualPrepareRequest request = CasualPrepareRequest.newBuilder()
                .setExecution(MessageCreator.toUUID4(execution))
                .setXid(MessageCreator.toXID(xid))
                .setResourceManagerId(resource)
                .setXaFlags(flag.getFlagValue())
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.PREPARE_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setPrepare(request)
                .build()

        when:
        instance.prepareRequest( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())

        CasualReply replyEnvelope = inboundHandler.getMsg()
        CasualPrepareReply reply = replyEnvelope.getPrepare()

        then:
        1 * xaTerminator.prepare( xid ) >> {
            throw new XAException( XAReturnCode.XAER_RMERR.getId() )
        }

        reply != null
        replyEnvelope.getMessageType() == CasualReply.MessageType.PREPARE_REPLY
        MessageCreator.toUUID(replyEnvelope.getCorrelationId()) == correlationId
        MessageCreator.toUUID(reply.getExecution()) == execution
        XAReturnCode.valueOf(reply.getXaReturnCode().name()) == XAReturnCode.XAER_RMERR
    }

    def "CommitRequest"()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()

        CasualCommitRequest request = CasualCommitRequest.newBuilder()
                .setExecution(MessageCreator.toUUID4(execution))
                .setXid(MessageCreator.toXID(xid))
                .setResourceManagerId(resource)
                .setXaFlags(flag.getFlagValue())
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.COMMIT_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setCommit(request)
                .build()

        when:
        instance.commitRequest( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())

        CasualReply replyEnvelope = inboundHandler.getMsg()
        CasualCommitReply reply = replyEnvelope.getCommit()

        then:
        1 * xaTerminator.commit( xid, false )

        reply != null
        replyEnvelope.getMessageType() == CasualReply.MessageType.COMMIT_REPLY
        MessageCreator.toUUID(replyEnvelope.getCorrelationId()) == correlationId
        MessageCreator.toUUID(reply.getExecution()) == execution
        XAReturnCode.valueOf(reply.getXaReturnCode().name()) == XAReturnCode.XA_OK
    }

    def "CommitRequest one phase true"()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of( XAFlags.TMONEPHASE )

        CasualCommitRequest request = CasualCommitRequest.newBuilder()
                .setExecution(MessageCreator.toUUID4(execution))
                .setXid(MessageCreator.toXID(xid))
                .setResourceManagerId(resource)
                .setXaFlags(flag.getFlagValue())
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.COMMIT_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setCommit(request)
                .build()

        when:
        instance.commitRequest( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())

        CasualReply replyEnvelope = inboundHandler.getMsg()
        CasualCommitReply reply = replyEnvelope.getCommit()

        then:
        1 * xaTerminator.commit( xid, true )

        reply != null
        replyEnvelope.getMessageType() == CasualReply.MessageType.COMMIT_REPLY
        MessageCreator.toUUID(replyEnvelope.getCorrelationId()) == correlationId
        MessageCreator.toUUID(reply.getExecution()) == execution
        XAReturnCode.valueOf(reply.getXaReturnCode().name()) == XAReturnCode.XA_OK
    }

    def "CommitRequest commit throws exception, returns exception error code."()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()

        CasualCommitRequest request = CasualCommitRequest.newBuilder()
                .setExecution(MessageCreator.toUUID4(execution))
                .setXid(MessageCreator.toXID(xid))
                .setResourceManagerId(resource)
                .setXaFlags(flag.getFlagValue())
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.COMMIT_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setCommit(request)
                .build()

        when:
        instance.commitRequest( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())

        CasualReply replyEnvelope = inboundHandler.getMsg()
        CasualCommitReply reply = replyEnvelope.getCommit()

        then:
        1 * xaTerminator.commit( xid, false ) >> {
            throw new XAException( XAReturnCode.XAER_RMERR.getId() )
        }

        reply != null
        replyEnvelope.getMessageType() == CasualReply.MessageType.COMMIT_REPLY
        MessageCreator.toUUID(replyEnvelope.getCorrelationId()) == correlationId
        MessageCreator.toUUID(reply.getExecution()) == execution
        XAReturnCode.valueOf(reply.getXaReturnCode().name()) == XAReturnCode.XAER_RMERR
    }

    def "RollbackRequest"()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()

        CasualRollbackRequest request = CasualRollbackRequest.newBuilder()
                .setExecution(MessageCreator.toUUID4(execution))
                .setXid(MessageCreator.toXID(xid))
                .setResourceManagerId(resource)
                .setXaFlags(flag.getFlagValue())
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.ROLLBACK_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setRollback(request)
                .build()
        when:
        instance.requestRollback( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())

        CasualReply replyEnvelope = inboundHandler.getMsg()
        CasualRollbackReply reply = replyEnvelope.getRollback()

        then:
        1 * xaTerminator.rollback( xid )

        reply != null
        replyEnvelope.getMessageType() == CasualReply.MessageType.ROLLBACK_REPLY
        MessageCreator.toUUID(replyEnvelope.getCorrelationId()) == correlationId
        MessageCreator.toUUID(reply.getExecution()) == execution
        XAReturnCode.valueOf(reply.getXaReturnCode().name()) == XAReturnCode.XA_OK
    }

    def "RollbackRequest rollback throws exception, returns exception error code."()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()

        CasualRollbackRequest request = CasualRollbackRequest.newBuilder()
                .setExecution(MessageCreator.toUUID4(execution))
                .setXid(MessageCreator.toXID(xid))
                .setResourceManagerId(resource)
                .setXaFlags(flag.getFlagValue())
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.ROLLBACK_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setRollback(request)
                .build()

        when:
        instance.requestRollback( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())

        CasualReply replyEnvelope = inboundHandler.getMsg()
        CasualRollbackReply reply = replyEnvelope.getRollback()

        then:
        1 * xaTerminator.rollback( xid ) >> {
            throw new XAException( XAReturnCode.XAER_RMERR.getId() )
        }

        reply != null
        replyEnvelope.getMessageType() == CasualReply.MessageType.ROLLBACK_REPLY
        MessageCreator.toUUID(replyEnvelope.getCorrelationId()) == correlationId
        MessageCreator.toUUID(reply.getExecution()) == execution
        XAReturnCode.valueOf(reply.getXaReturnCode().name()) == XAReturnCode.XAER_RMERR
    }

    List<Service> toServiceList(List<se.laz.casual.network.messages.Service> services)
    {
        return services.stream()
                .map({s -> Service.of(s.getName(), s.getCategory(), MessageCreator.toTransactionType(s.getTransactionType()))
                        .setHops(s.getHops())
                        .setTimeout(s.getTimeout())})
                    .collect(Collectors.toList())
    }

    class TestCasualService implements CasualService{
        @Override
        String name()
        {
            return serviceName
        }

        @Override
        String category()
        {
            return "mycategory"
        }

        @Override
        Class<? extends Annotation> annotationType()
        {
            return CasualService.class
        }
    }
}
