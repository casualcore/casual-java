package se.kodarkatten.casual.jca.inflow

import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.api.flags.XAFlags
import se.kodarkatten.casual.api.xa.XAReturnCode
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.jca.CasualResourceAdapterException
import se.kodarkatten.casual.jca.inflow.work.CasualServiceCallWork
import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader
import se.kodarkatten.casual.network.messages.CasualNWMessageType
import se.kodarkatten.casual.network.messages.domain.*
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.messages.service.ServiceBuffer
import se.kodarkatten.casual.network.messages.transaction.*
import se.kodarkatten.casual.network.utils.LocalEchoSocketChannel
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.XATerminator
import javax.resource.spi.work.ExecutionContext
import javax.resource.spi.work.WorkException
import javax.resource.spi.work.WorkListener
import javax.resource.spi.work.WorkManager
import javax.transaction.xa.XAException
import javax.transaction.xa.Xid
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.util.concurrent.ThreadLocalRandom

class CasualMessageListenerImplTest extends Specification
{
    @Shared CasualMessageListener instance
    @Shared SocketChannel channel
    @Shared WorkManager workManager
    @Shared XATerminator xaTerminator

    @Shared UUID correlationId = UUID.randomUUID()
    @Shared UUID domainId = UUID.randomUUID()
    @Shared String domainName = "java"
    @Shared UUID execution = UUID.randomUUID()
    @Shared Xid xid


    def setup()
    {
        instance = new CasualMessageListenerImpl()
        channel = new LocalEchoSocketChannel()
        workManager = Mock( WorkManager )
        xaTerminator = Mock( XATerminator )

        xid = createXid()
    }

    Xid createXid()
    {
        String gid = Integer.toString( ThreadLocalRandom.current().nextInt() );
        String b = Integer.toString( ThreadLocalRandom.current().nextInt() );
        return XID.of(gid.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8), 0);
    }


    def "DomainConnectRequest"()
    {
        given:
        long protocolVersion = 1000L
        CasualNWMessage<CasualDomainConnectRequestMessage> message = CasualNWMessage.of( correlationId,
                CasualDomainConnectRequestMessage.createBuilder()
                        .withDomainId( domainId )
                        .withDomainName(domainName )
                        .withExecution( execution )
                        .withProtocols(Arrays.asList(protocolVersion))
                        .build()
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.domainConnectRequest( header, channel )
        CasualNWMessage<CasualDomainConnectReplyMessage> reply = CasualNetworkReader.read( channel )

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
        List<String> serviceNames = Arrays.asList( "echo" )
        List<Service> expectedServices = Arrays.asList( Service.of( "echo", "", TransactionType.NONE ) )
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> message = CasualNWMessage.of( correlationId,
                CasualDomainDiscoveryRequestMessage.createBuilder()
                        .setDomainId( domainId )
                        .setDomainName(domainName )
                        .setExecution( execution )
                        .setServiceNames( serviceNames )
                        .build()
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.domainDiscoveryRequest( header, channel )
        CasualNWMessage<CasualDomainDiscoveryReplyMessage> reply = CasualNetworkReader.read( channel )

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
        CasualNWMessage<CasualServiceCallRequestMessage> message = CasualNWMessage.of( correlationId,
                CasualServiceCallRequestMessage.createBuilder()
                        .setXid( xid )
                        .setExecution(execution)
                        .setServiceName( serviceName )
                        .setServiceBuffer( ServiceBuffer.of( "json", JsonBuffer.of( "{\"hello\"}").getBytes() ) )
                        .setXatmiFlags( Flag.of())
                        .setTimeout( timeout )
                        .build()
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.serviceCallRequest( header, channel, workManager )

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
        actualWork.getHeader() == header
        actualWork.getSocketChannel() == channel
        actualStartTimeout != null
        actualStartTimeout == WorkManager.INDEFINITE
        actualExecutionContext != null
        actualExecutionContext.getXid() == xid
        actualExecutionContext.getTransactionTimeout() == timeout
        actualWorkListener == null
    }

    def "ServiceCallRequest with null xid calls service work without transaction context."()
    {
        given:
        xid = XID.NULL_XID
        CasualServiceCallWork actualWork

        String serviceName = "echo"
        CasualNWMessage<CasualServiceCallRequestMessage> message = CasualNWMessage.of( correlationId,
                CasualServiceCallRequestMessage.createBuilder()
                        .setXid( xid )
                        .setExecution(execution)
                        .setServiceName( serviceName )
                        .setServiceBuffer( ServiceBuffer.of( "json", JsonBuffer.of( "{\"hello\"}").getBytes() ) )
                        .setXatmiFlags( Flag.of())
                        .build()
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.serviceCallRequest( header, channel, workManager )

        then:
        1 * workManager.startWork( _) >> {
            CasualServiceCallWork work ->
                actualWork = work
                return 1L
        }

        actualWork != null
        actualWork.getHeader() == header
        actualWork.getSocketChannel() == channel
    }

    def "ServiceCallRequest startWork throws exception, wrapped and thrown."()
    {
        given:
        String serviceName = "echo"
        CasualNWMessage<CasualServiceCallRequestMessage> message = CasualNWMessage.of( correlationId,
                CasualServiceCallRequestMessage.createBuilder()
                        .setXid( xid )
                        .setExecution(execution)
                        .setServiceName( serviceName )
                        .setServiceBuffer( ServiceBuffer.of( "json", JsonBuffer.of( "{\"hello\"}").getBytes() ) )
                        .setXatmiFlags( Flag.of())
                        .build()
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.serviceCallRequest( header, channel, workManager )

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
        CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> message = CasualNWMessage.of(correlationId,
                CasualTransactionResourcePrepareRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.prepareRequest( header, channel, xaTerminator )
        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> reply = CasualNetworkReader.read( channel )

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
        CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> message = CasualNWMessage.of(correlationId,
                CasualTransactionResourcePrepareRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.prepareRequest( header, channel, xaTerminator )
        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> reply = CasualNetworkReader.read( channel )

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
        CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> message = CasualNWMessage.of(correlationId,
                CasualTransactionResourcePrepareRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.prepareRequest( header, channel, xaTerminator )
        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> reply = CasualNetworkReader.read( channel )

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
        CasualNWMessage<CasualTransactionResourceCommitRequestMessage> message = CasualNWMessage.of(correlationId,
                CasualTransactionResourceCommitRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.commitRequest( header, channel, xaTerminator )
        CasualNWMessage<CasualTransactionResourceCommitReplyMessage> reply = CasualNetworkReader.read( channel )

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
        CasualNWMessage<CasualTransactionResourceCommitRequestMessage> message = CasualNWMessage.of(correlationId,
                CasualTransactionResourceCommitRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.commitRequest( header, channel, xaTerminator )
        CasualNWMessage<CasualTransactionResourceCommitReplyMessage> reply = CasualNetworkReader.read( channel )

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
        CasualNWMessage<CasualTransactionResourceCommitRequestMessage> message = CasualNWMessage.of(correlationId,
                CasualTransactionResourceCommitRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.commitRequest( header, channel, xaTerminator )
        CasualNWMessage<CasualTransactionResourceCommitReplyMessage> reply = CasualNetworkReader.read( channel )

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
        CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> message = CasualNWMessage.of(correlationId,
                CasualTransactionResourceRollbackRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.requestRollback( header, channel, xaTerminator )
        CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> reply = CasualNetworkReader.read( channel )

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
        CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> message = CasualNWMessage.of(correlationId,
                CasualTransactionResourceRollbackRequestMessage.of(
                        execution,
                        xid,
                        resource,
                        flag
                )
        )
        CasualNetworkWriter.write(channel, message)

        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader( channel )

        when:
        instance.requestRollback( header, channel, xaTerminator )
        CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> reply = CasualNetworkReader.read( channel )

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
}
