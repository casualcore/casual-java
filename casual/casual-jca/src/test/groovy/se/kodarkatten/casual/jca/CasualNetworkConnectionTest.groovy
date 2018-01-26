package se.kodarkatten.casual.jca

import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.utils.LocalEchoSocketChannel
import spock.lang.Shared
import spock.lang.Specification

import java.nio.channels.SocketChannel

class CasualNetworkConnectionTest extends Specification
{
    @Shared CasualNetworkConnection instance
    @Shared SocketChannel testChannel
    @Shared ManagedConnectionInvalidator invalidator
    @Shared CasualNetworkConnectionInformation connectionInformation

    def setup()
    {
        invalidator = Mock(ManagedConnectionInvalidator)
        connectionInformation = CasualNetworkConnectionInformation.of(new InetSocketAddress(3712), 1000l, UUID.randomUUID(), 'testDomain')
        testChannel = new LocalEchoSocketChannel()
        instance = new CasualNetworkConnection( testChannel , connectionInformation, invalidator)
    }

    def "Of with a null InetSocketAddress throws NullPointerException."()
    {
        when:
        CasualNetworkConnection.of( null , null)

        then:
        thrown NullPointerException
    }

    def "RequestReply message is written and then read back from the test socket."()
    {
        setup:
        CasualNWMessage<CasualServiceCallRequestMessage> m = createRequestMessage()

        when:
        CasualNWMessage<CasualServiceCallRequestMessage> reply = instance.requestReply( m )

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
        def nwc = new CasualNetworkConnection( channel , connectionInformation, invalidator)
        when:
        channel.write(_) >> {throw exception}
        nwc.requestReply(msg)
        then:
        thrown(CasualTransportException)
        1 * invalidator.invalidate()
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
