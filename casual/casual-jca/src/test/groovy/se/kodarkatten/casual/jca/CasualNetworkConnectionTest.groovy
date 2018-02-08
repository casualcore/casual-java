package se.kodarkatten.casual.jca

import se.kodarkatten.casual.jca.message.impl.CorrelatorImpl
import se.kodarkatten.casual.jca.work.NetworkReader
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.utils.DummyWorkManager
import se.kodarkatten.casual.network.utils.LocalEchoSocketChannel
import spock.lang.Shared
import spock.lang.Specification

import java.nio.channels.SocketChannel
import java.util.concurrent.CompletableFuture

class CasualNetworkConnectionTest extends Specification
{
    @Shared CasualNetworkConnection instance
    @Shared LocalEchoSocketChannel testChannel
    @Shared ManagedConnectionInvalidator invalidator
    @Shared CasualNetworkConnectionInformation connectionInformation
    @Shared NetworkReader reader
    @Shared DummyWorkManager workManager = DummyWorkManager.of()

    def setup()
    {
        invalidator = Mock(ManagedConnectionInvalidator)
        connectionInformation = CasualNetworkConnectionInformation.of(new InetSocketAddress(3712), 1000l, UUID.randomUUID(), 'testDomain')
        testChannel = new LocalEchoSocketChannel()
        CorrelatorImpl correlator = CorrelatorImpl.of()
        reader = NetworkReader.of(correlator, testChannel, invalidator)
        instance = new CasualNetworkConnection( testChannel , connectionInformation, invalidator, reader , correlator)
    }

    def cleanup()
    {
        workManager.done()
    }

    def "Of with a null InetSocketAddress throws NullPointerException."()
    {
        when:
        CasualNetworkConnection.of( null , null, null)

        then:
        thrown NullPointerException
    }

    def "RequestReply message is written and then read back from the test socket."()
    {
        setup:
        CasualNWMessage<CasualServiceCallRequestMessage> m = createRequestMessage()

        when:
        CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>> f = instance.request( m )
        workManager.startWork(reader)
        CasualNWMessage<CasualServiceCallReplyMessage> reply = f.get()
        then:
        reply == m
    }

    def "Close performs a close on the socket."()
    {
        when:
        instance.close()

        then:
        noExceptionThrown()
    }

    def 'simulate connected casual instance gone'()
    {
        setup:
        def exception = new CasualTransportException('bazinga', new IOException())
        def msg = createRequestMessage()
        def channel = Stub(SocketChannel)
        def reader = GroovyMock(NetworkReader)
        def correlator = CorrelatorImpl.of()
        def nwc = new CasualNetworkConnection( channel , connectionInformation, invalidator, reader, correlator)
        when:
        channel.write(_) >> {throw exception}
        nwc.request(msg)
        then:
        def e = thrown(CasualTransportException)
        e == exception
        1 * invalidator.invalidate(exception)
        correlator.isEmpty()
    }

    def createRequestMessage()
    {
        CasualDomainDiscoveryRequestMessage message = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setDomainId(UUID.randomUUID())
                .setDomainName( "test-domain" )
                .setServiceNames(Arrays.asList("echo"))
                .build()
        CasualNWMessage<CasualServiceCallRequestMessage> m = CasualNWMessage.of( UUID.randomUUID(), message )
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains "CasualNetworkConnection"
    }

}
