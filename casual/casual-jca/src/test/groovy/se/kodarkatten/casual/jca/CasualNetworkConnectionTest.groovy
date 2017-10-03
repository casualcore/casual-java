package se.kodarkatten.casual.jca

import se.kodarkatten.casual.jca.CasualNetworkConnection
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.utils.LocalEchoSocketChannel
import spock.lang.Shared
import spock.lang.Specification

import java.nio.channels.SocketChannel

class CasualNetworkConnectionTest extends Specification
{
    @Shared CasualNetworkConnection instance
    @Shared SocketChannel testChannel

    def setup()
    {
        testChannel = new LocalEchoSocketChannel()

        instance = new CasualNetworkConnection( testChannel )
    }

    def "Of with a null InetSocketAddress throws NullPointerException."()
    {
        when:
        CasualNetworkConnection.of( null )

        then:
        thrown NullPointerException
    }

    def "RequestReply message is written and then read back from the test socket."()
    {
        setup:
        CasualDomainDiscoveryRequestMessage message = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setDomainId(UUID.randomUUID())
                .setDomainName( "test-domain" )
                .setServiceNames(Arrays.asList("echo"))
                .build()
        CasualNWMessage<CasualServiceCallRequestMessage> m = CasualNWMessage.of( UUID.randomUUID(), message )

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

    def "toString test."()
    {
        expect:
        instance.toString().contains "CasualNetworkConnection"
    }
}
