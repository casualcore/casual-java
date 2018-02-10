package se.kodarkatten.casual.jca.inflow.work

import se.kodarkatten.casual.jca.CasualResourceAdapterException
import se.kodarkatten.casual.jca.inflow.CasualActivationSpec
import se.kodarkatten.casual.network.io.LockableSocketChannel
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.XATerminator
import javax.resource.spi.endpoint.MessageEndpointFactory
import javax.resource.spi.work.WorkException
import javax.resource.spi.work.WorkManager
import java.nio.channels.SocketChannel

class SocketChannelConsumerTest extends Specification
{

    @Shared SocketChannelConsumer instance
    @Shared CasualInboundWork inboundWork
    @Shared CasualActivationSpec spec
    @Shared MessageEndpointFactory factory
    @Shared WorkManager workManager
    @Shared XATerminator xaTerminator
    @Shared InetSocketAddress okAddress = new InetSocketAddress(0)
    @Shared LockableSocketChannel channel
    @Shared SocketChannel socketChannel


    def setup()
    {
        spec = new CasualActivationSpec()
        spec.setPort( okAddress.getPort() )
        factory = Mock( MessageEndpointFactory )
        workManager = Mock( WorkManager )
        xaTerminator = Mock( XATerminator )
        socketChannel = Mock( SocketChannel )
        channel = LockableSocketChannel.of( socketChannel )

        inboundWork = new CasualInboundWork( spec, factory, workManager, xaTerminator )

        instance = new SocketChannelConsumer( inboundWork )
    }

    def "Accept calls startWork throws exception, wraps and throws."()
    {
        given:
        1 * workManager.startWork( _ ) >>  {
            throw new WorkException( "Fake error." )
        }

        when:
        instance.accept( channel )

        then:
        thrown CasualResourceAdapterException
    }

    def "Accept starts CasualSocketWork with params forwarded."()
    {
        given:
        CasualSocketWork actual

        when:
        instance.accept( channel )

        then:
        1 * workManager.startWork( _ ) >> {
            CasualSocketWork input ->
                actual = input
                return 1L
        }
        actual != null
        actual.getSocketChannel() != null
        actual.getWork() == inboundWork
    }
}
