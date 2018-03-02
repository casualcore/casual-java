package se.kodarkatten.casual.jca.inflow

import io.netty.channel.embedded.EmbeddedChannel
import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.api.flags.XAFlags
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessageType
import se.kodarkatten.casual.api.service.CasualService
import se.kodarkatten.casual.api.xa.XAReturnCode
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.jca.CasualResourceAdapterException
import se.kodarkatten.casual.jca.inbound.handler.service.casual.CasualServiceMetaData
import se.kodarkatten.casual.jca.inbound.handler.service.casual.CasualServiceRegistry
import se.kodarkatten.casual.jca.inflow.work.CasualServiceCallWork
import se.kodarkatten.casual.network.messages.domain.TransactionType
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.messages.domain.*
import se.kodarkatten.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer
import se.kodarkatten.casual.network.protocol.messages.transaction.*
import se.laz.casual.network.CasualNWMessageDecoder
import se.laz.casual.network.CasualNWMessageEncoder
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
import java.util.concurrent.ThreadLocalRandom

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
        channel = new EmbeddedChannel(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), inboundHandler)
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
        CasualNWMessageImpl<CasualDomainConnectRequestMessage> message = CasualNWMessageImpl.of( correlationId,
                CasualDomainConnectRequestMessage.createBuilder()
                        .withDomainId( domainId )
                        .withDomainName(domainName )
                        .withExecution( execution )
                        .withProtocols(Arrays.asList(protocolVersion))
                        .build()
        )

        when:
        instance.domainConnectRequest( message, channel )
        channel.writeInbound(channel.outboundMessages().element())
        CasualNWMessage<CasualDomainConnectReplyMessage> reply = inboundHandler.getMsg()

        then:
        reply != null
        reply.getType() == CasualNWMessageType.DOMAIN_CONNECT_REPLY
        reply.getCorrelationId() == correlationId
        reply.getMessage().getDomainId() == domainId
        reply.getMessage().getDomainName() == domainName
        reply.getMessage().getExecution() == execution
        reply.getMessage().getProtocolVersion() == protocolVersion
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
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> message = CasualNWMessageImpl.of( correlationId,
                CasualDomainDiscoveryRequestMessage.createBuilder()
                        .setDomainId( domainId )
                        .setDomainName(domainName )
                        .setExecution( execution )
                        .setServiceNames( serviceNames )
                        .build()
        )

        when:
        instance.domainDiscoveryRequest( message, channel )
        channel.writeInbound(channel.outboundMessages().element())
        CasualNWMessage<CasualDomainDiscoveryReplyMessage> reply = inboundHandler.getMsg()

        then:
        reply != null
        reply.getType() == CasualNWMessageType.DOMAIN_DISCOVERY_REPLY
        reply.getCorrelationId() == correlationId
        reply.getMessage().getDomainId() == domainId
        reply.getMessage().getDomainName() == domainName
        reply.getMessage().getExecution() == execution
        reply.getMessage().getServices() == expectedServices
    }

    def "ServiceCallRequest"()
    {
        given:
        CasualServiceCallWork actualWork
        long actualStartTimeout
        ExecutionContext actualExecutionContext
        WorkListener actualWorkListener
        long timeout = 12L
        String serviceName = "echo"
        CasualNWMessageImpl<CasualServiceCallRequestMessage> message = CasualNWMessageImpl.of( correlationId,
                CasualServiceCallRequestMessage.createBuilder()
                        .setXid( xid )
                        .setExecution(execution)
                        .setServiceName( serviceName )
                        .setServiceBuffer( ServiceBuffer.of( "json", JsonBuffer.of( "{\"hello\"}").getBytes() ) )
                        .setXatmiFlags( Flag.of())
                        .setTimeout( timeout )
                        .build()
        )

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
        CasualNWMessageImpl<CasualServiceCallRequestMessage> message = CasualNWMessageImpl.of( correlationId,
                CasualServiceCallRequestMessage.createBuilder()
                        .setXid( xid )
                        .setExecution(execution)
                        .setServiceName( serviceName )
                        .setServiceBuffer( ServiceBuffer.of( "json", JsonBuffer.of( "{\"hello\"}").getBytes() ) )
                        .setXatmiFlags( Flag.of())
                        .build()
        )

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
        CasualNWMessageImpl<CasualServiceCallRequestMessage> message = CasualNWMessageImpl.of( correlationId,
                CasualServiceCallRequestMessage.createBuilder()
                        .setXid( xid )
                        .setExecution(execution)
                        .setServiceName( serviceName )
                        .setServiceBuffer( ServiceBuffer.of( "json", JsonBuffer.of( "{\"hello\"}").getBytes() ) )
                        .setXatmiFlags( Flag.of())
                        .build()
        )

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
        CasualNWMessageImpl<CasualTransactionResourcePrepareRequestMessage> message = CasualNWMessageImpl.of(correlationId,
                CasualTransactionResourcePrepareRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )

        when:
        instance.prepareRequest( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())
        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> reply = inboundHandler.getMsg()

        then:
        1 * xaTerminator.prepare( xid ) >> {
            return XAReturnCode.XA_OK.getId()
        }

        reply != null
        reply.getType() == CasualNWMessageType.PREPARE_REQUEST_REPLY
        reply.getCorrelationId() == correlationId
        reply.getMessage().getExecution() == execution
        reply.getMessage().getTransactionReturnCode() == XAReturnCode.XA_OK
    }

    def "PrepareRequest return code failure."()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()
        CasualNWMessageImpl<CasualTransactionResourcePrepareRequestMessage> message = CasualNWMessageImpl.of(correlationId,
                CasualTransactionResourcePrepareRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )

        when:
        instance.prepareRequest( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())
        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> reply = inboundHandler.getMsg()

        then:
        1 * xaTerminator.prepare( xid ) >> {
            return XAReturnCode.XAER_RMERR.getId()
        }

        reply != null
        reply.getType() == CasualNWMessageType.PREPARE_REQUEST_REPLY
        reply.getCorrelationId() == correlationId
        reply.getMessage().getExecution() == execution
        reply.getMessage().getTransactionReturnCode() == XAReturnCode.XAER_RMERR
    }

    def "PrepareRequest prepare throws exception, returns exception error code."()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()
        CasualNWMessageImpl<CasualTransactionResourcePrepareRequestMessage> message = CasualNWMessageImpl.of(correlationId,
                CasualTransactionResourcePrepareRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )

        when:
        instance.prepareRequest( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())
        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> reply = inboundHandler.getMsg()

        then:
        1 * xaTerminator.prepare( xid ) >> {
            throw new XAException( XAReturnCode.XAER_RMERR.getId() )
        }

        reply != null
        reply.getType() == CasualNWMessageType.PREPARE_REQUEST_REPLY
        reply.getCorrelationId() == correlationId
        reply.getMessage().getExecution() == execution
        reply.getMessage().getTransactionReturnCode() == XAReturnCode.XAER_RMERR
    }

    def "CommitRequest"()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()
        CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> message = CasualNWMessageImpl.of(correlationId,
                CasualTransactionResourceCommitRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )

        when:
        instance.commitRequest( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())
        CasualNWMessage<CasualTransactionResourceCommitReplyMessage> reply = inboundHandler.getMsg()

        then:
        1 * xaTerminator.commit( xid, false )

        reply != null
        reply.getType() == CasualNWMessageType.COMMIT_REQUEST_REPLY
        reply.getCorrelationId() == correlationId
        reply.getMessage().getExecution() == execution
        reply.getMessage().getTransactionReturnCode() == XAReturnCode.XA_OK
    }

    def "CommitRequest one phase true"()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of( XAFlags.TMONEPHASE )
        CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> message = CasualNWMessageImpl.of(correlationId,
                CasualTransactionResourceCommitRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )

        when:
        instance.commitRequest( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())
        CasualNWMessage<CasualTransactionResourceCommitReplyMessage> reply = inboundHandler.getMsg()

        then:
        1 * xaTerminator.commit( xid, true )

        reply != null
        reply.getType() == CasualNWMessageType.COMMIT_REQUEST_REPLY
        reply.getCorrelationId() == correlationId
        reply.getMessage().getExecution() == execution
        reply.getMessage().getTransactionReturnCode() == XAReturnCode.XA_OK
    }

    def "CommitRequest commit throws exception, returns exception error code."()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()
        CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> message = CasualNWMessageImpl.of(correlationId,
                CasualTransactionResourceCommitRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )

        when:
        instance.commitRequest( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())
        CasualNWMessage<CasualTransactionResourceCommitReplyMessage> reply = inboundHandler.getMsg()

        then:
        1 * xaTerminator.commit( xid, false ) >> {
            throw new XAException( XAReturnCode.XAER_RMERR.getId() )
        }

        reply != null
        reply.getType() == CasualNWMessageType.COMMIT_REQUEST_REPLY
        reply.getCorrelationId() == correlationId
        reply.getMessage().getExecution() == execution
        reply.getMessage().getTransactionReturnCode() == XAReturnCode.XAER_RMERR
    }

    def "RollbackRequest"()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()
        CasualNWMessageImpl<CasualTransactionResourceRollbackRequestMessage> message = CasualNWMessageImpl.of(correlationId,
                CasualTransactionResourceRollbackRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )

        when:
        instance.requestRollback( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())
        CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> reply = inboundHandler.getMsg()

        then:
        1 * xaTerminator.rollback( xid )

        reply != null
        reply.getType() == CasualNWMessageType.REQUEST_ROLLBACK_REPLY
        reply.getCorrelationId() == correlationId
        reply.getMessage().getExecution() == execution
        reply.getMessage().getTransactionReturnCode() == XAReturnCode.XA_OK
    }

    def "RollbackRequest rollback throws exception, returns exception error code."()
    {
        given:
        int resource = 1
        Flag<XAFlags> flag = Flag.of()
        CasualNWMessageImpl<CasualTransactionResourceRollbackRequestMessage> message = CasualNWMessageImpl.of(correlationId,
                CasualTransactionResourceRollbackRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )

        when:
        instance.requestRollback( message, channel, xaTerminator )
        channel.writeInbound(channel.outboundMessages().element())
        CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> reply = inboundHandler.getMsg()

        then:
        1 * xaTerminator.rollback( xid ) >> {
            throw new XAException( XAReturnCode.XAER_RMERR.getId() )
        }

        reply != null
        reply.getType() == CasualNWMessageType.REQUEST_ROLLBACK_REPLY
        reply.getCorrelationId() == correlationId
        reply.getMessage().getExecution() == execution
        reply.getMessage().getTransactionReturnCode() == XAReturnCode.XAER_RMERR
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
