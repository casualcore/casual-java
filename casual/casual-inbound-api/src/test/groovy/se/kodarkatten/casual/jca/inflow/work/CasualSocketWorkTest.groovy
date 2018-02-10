package se.kodarkatten.casual.jca.inflow.work

import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.api.xa.XAReturnCode
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.jca.CasualResourceAdapterException
import se.kodarkatten.casual.jca.inflow.CasualActivationSpec
import se.kodarkatten.casual.jca.inflow.CasualMessageListener
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.io.LockableSocketChannel
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader
import se.kodarkatten.casual.network.messages.CasualNWMessageType
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable
import se.kodarkatten.casual.network.messages.domain.CasualDomainConnectRequestMessage
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.messages.service.ServiceBuffer
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceCommitReplyMessage
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceCommitRequestMessage
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourcePrepareRequestMessage
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceRollbackRequestMessage
import se.kodarkatten.casual.network.utils.LocalEchoSocketChannel
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.UnavailableException
import javax.resource.spi.XATerminator
import javax.resource.spi.endpoint.MessageEndpoint
import javax.resource.spi.endpoint.MessageEndpointFactory
import javax.resource.spi.work.HintsContext
import javax.resource.spi.work.WorkContext
import javax.resource.spi.work.WorkManager
import java.nio.channels.SocketChannel

class CasualSocketWorkTest extends Specification
{
    @Shared CasualSocketWork instance
    @Shared CasualInboundWork inboundWork
    @Shared CasualActivationSpec spec
    @Shared MessageEndpointFactory factory
    @Shared WorkManager workManager
    @Shared XATerminator xaTerminator
    @Shared InetSocketAddress okAddress = new InetSocketAddress(0)

    @Shared LockableSocketChannel channel
    @Shared SocketChannel socketChannel

    @Shared CasualNWMessage<? extends CasualNetworkTransmittable> actualMessage
    @Shared LockableSocketChannel actualChannel
    @Shared WorkManager actualWorkManager
    @Shared XATerminator actualXATerminator
    @Shared CasualMessageListener mdb

    def setup()
    {
        spec = new CasualActivationSpec()
        spec.setPort( okAddress.getPort() )
        factory = Mock( MessageEndpointFactory )
        workManager = Mock( WorkManager )
        xaTerminator = Mock( XATerminator )
        inboundWork = new CasualInboundWork( spec, factory, workManager, xaTerminator )

        socketChannel = new LocalEchoSocketChannel()
        channel = LockableSocketChannel.of( socketChannel )

        instance = new CasualSocketWork( channel, inboundWork )

        mdb = Mock( MdbListener )

        factory.createEndpoint( _ ) >> {
            return mdb
        }
    }

    def "Domain Connect Request forwarded."()
    {
        given:
        CasualNWMessage<CasualDomainConnectRequestMessage> message = CasualNWMessage.of(UUID.randomUUID(),
                CasualDomainConnectRequestMessage.createBuilder()
                        .withDomainId(UUID.randomUUID())
                        .withDomainName("java")
                        .withExecution(UUID.randomUUID())
                        .withProtocols(Arrays.asList(1000L))
                        .build()
        )
        CasualNetworkWriter.write(channel.getSocketChannel(), message)

        1 * mdb.domainConnectRequest( _ , _ ) >> { CasualNWMessage m, LockableSocketChannel c ->
            actualMessage = m
            actualChannel = c
            disconnect( channel )
        }

        when:
        instance.run()

        then:
        actualMessage.getType() == CasualNWMessageType.DOMAIN_CONNECT_REQUEST
        actualChannel == channel
    }

    def "Domain Discovery Request forwarded."()
    {
        given:
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> message = CasualNWMessage.of(UUID.randomUUID(),
                CasualDomainDiscoveryRequestMessage.createBuilder()
                        .setDomainId(UUID.randomUUID())
                        .setDomainName("java")
                        .setExecution(UUID.randomUUID())
                        .setServiceNames( Arrays.asList( "echo" ))
                        .build()
        )
        CasualNetworkWriter.write(channel, message)

        1 * mdb.domainDiscoveryRequest( _ , _ ) >> { CasualNWMessage m, LockableSocketChannel c ->
            actualMessage = m
            actualChannel = c
            disconnect( channel )
        }

        when:
        instance.run()

        then:
        actualMessage.getType() == CasualNWMessageType.DOMAIN_DISCOVERY_REQUEST
        actualChannel == channel
    }

    def "Service Call Request forwarded."()
    {
        given:
        CasualNWMessage<CasualServiceCallRequestMessage> message = CasualNWMessage.of(UUID.randomUUID(),
                CasualServiceCallRequestMessage.createBuilder()
                        .setXid( XID.NULL_XID)
                        .setExecution(UUID.randomUUID())
                        .setServiceName( "echo" )
                        .setServiceBuffer( ServiceBuffer.of( "json", JsonBuffer.of( "{\"hello\"}").getBytes() ) )
                        .setXatmiFlags( Flag.of())
                        .build()
        )
        CasualNetworkWriter.write(channel, message)

        1 * mdb.serviceCallRequest( _ , _, _ ) >> { CasualNWMessage m, LockableSocketChannel c, WorkManager wm ->
            actualMessage = m
            actualChannel = c
            actualWorkManager = wm
            disconnect( channel )
        }

        when:
        instance.run()

        then:
        actualMessage.getType() == CasualNWMessageType.SERVICE_CALL_REQUEST
        actualChannel == channel
        actualWorkManager == workManager
    }

    def "Prepare Request forwarded."()
    {
        given:
        CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> message = CasualNWMessage.of(UUID.randomUUID(),
                CasualTransactionResourcePrepareRequestMessage.of(
                        UUID.randomUUID(),
                        XID.NULL_XID,
                        1,
                        Flag.of()
                )
        )
        CasualNetworkWriter.write(channel, message)

        1 * mdb.prepareRequest( _ , _, _ ) >> { CasualNWMessage m, LockableSocketChannel c, XATerminator xat ->
            actualMessage = m
            actualChannel = c
            actualXATerminator = xat
            disconnect( channel )
        }

        when:
        instance.run()

        then:
        actualMessage.getType() == CasualNWMessageType.PREPARE_REQUEST
        actualChannel == channel
        actualXATerminator == xaTerminator
    }

    def "Rollback Request forwarded."()
    {
        given:
        CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> message = CasualNWMessage.of(UUID.randomUUID(),
                CasualTransactionResourceRollbackRequestMessage.of(
                        UUID.randomUUID(),
                        XID.NULL_XID,
                        1,
                        Flag.of()
                )
        )
        CasualNetworkWriter.write(channel, message)

        1 * mdb.requestRollback( _ , _, _ ) >> { CasualNWMessage m, LockableSocketChannel c, XATerminator xat ->
            actualMessage = m
            actualChannel = c
            actualXATerminator = xat
            disconnect( channel )
        }

        when:
        instance.run()

        then:
        actualMessage.getType() == CasualNWMessageType.REQUEST_ROLLBACK
        actualChannel == channel
        actualXATerminator == xaTerminator
    }

    def "Commit Request forwarded."()
    {
        given:
        CasualNWMessage<CasualTransactionResourceCommitRequestMessage> message = CasualNWMessage.of(UUID.randomUUID(),
                CasualTransactionResourceCommitRequestMessage.of(
                        UUID.randomUUID(),
                        XID.NULL_XID,
                        1,
                        Flag.of()
                )
        )
        CasualNetworkWriter.write(channel, message)

        1 * mdb.commitRequest( _ , _, _ ) >> { CasualNWMessage m, LockableSocketChannel c, XATerminator xat ->
            actualMessage = m
            actualChannel = c
            actualXATerminator = xat
            disconnect( channel )
        }

        when:
        instance.run()

        then:
        actualMessage.getType() == CasualNWMessageType.COMMIT_REQUEST
        actualChannel == channel
        actualXATerminator == xaTerminator
    }

    def "Unknown request not forwarded."()
    {
        given:
        CasualNWMessage<CasualTransactionResourceCommitReplyMessage> message = CasualNWMessage.of(UUID.randomUUID(),
                CasualTransactionResourceCommitReplyMessage.of(
                        UUID.randomUUID(),
                        XID.NULL_XID,
                        1,
                        XAReturnCode.XA_OK
                )
        )
        CasualNetworkWriter.write(channel, message)

        when:
        instance.run()

        then:
        factory.createEndpoint( _ ) >> {
            disconnect( channel )
            return mdb
        }
        0 * mdb._()
    }

    def "Create message endpoint throws exception, throws wrapped exception."()
    {
        when:
        instance.run()

        then:
        factory.createEndpoint( _ ) >> {
            throw new UnavailableException( "Fake error." )
        }
        0 * mdb._()
        thrown CasualResourceAdapterException
    }

    def "Release work before run resulting in no forwarding."()
    {
        given:
        instance.release()

        when:
        instance.run()

        then:
        0 * factory.createEndpoint( _ )
        0 * mdb._()
    }

    def "Get WorkContext returns hint for long running."()
    {
        when:
        List<WorkContext> context = instance.getWorkContexts()
        HintsContext c = (HintsContext)context.get( 0 )

        then:
        context.size() == 1
        c.getHints().size() == 1
        c.getHints().get( HintsContext.LONGRUNNING_HINT ) == Boolean.TRUE
    }


    abstract class MdbListener implements CasualMessageListener, MessageEndpoint
    {

    }

    def disconnect( LockableSocketChannel channel )
    {
        ((LocalEchoSocketChannel)channel.getSocketChannel()).disconnect()
    }
}
