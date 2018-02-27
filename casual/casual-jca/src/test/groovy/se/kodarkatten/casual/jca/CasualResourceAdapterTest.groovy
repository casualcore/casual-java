package se.kodarkatten.casual.jca

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoop
import se.kodarkatten.casual.jca.inflow.CasualActivationSpec
import se.laz.casual.network.inbound.CasualServer
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.BootstrapContext
import javax.resource.spi.XATerminator
import javax.resource.spi.endpoint.MessageEndpointFactory
import javax.resource.spi.work.WorkManager

class CasualResourceAdapterTest extends Specification
{
    @Shared CasualResourceAdapter instance
    @Shared InetSocketAddress okAddress = new InetSocketAddress(0)

    def setup()
    {
        instance = new CasualResourceAdapter()
    }

    def "GetXAResources"()
    {
        expect:
        instance.getXAResources() == null
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains( "CasualResourceAdapter" )
    }

    def "start initialises WorkManager and XATerminator from context."()
    {
        setup:
        BootstrapContext context = Mock( BootstrapContext )
        WorkManager manager = Mock( WorkManager )
        XATerminator xat = Mock( XATerminator )
        context.getWorkManager() >> manager
        context.getXATerminator() >> xat

        when:
        instance.start( context )

        then:
        instance.getWorkManager() == manager
        instance.getXATerminator() == xat
    }

    def "Activate endpoint."()
    {
        setup:
        BootstrapContext context = Mock(BootstrapContext)
        WorkManager manager = Mock(WorkManager)
        XATerminator xat = Mock(XATerminator)
        context.getWorkManager() >> manager
        context.getXATerminator() >> xat
        instance.start(context)
        MessageEndpointFactory factory = Mock(MessageEndpointFactory)
        CasualActivationSpec spec = new CasualActivationSpec()
        spec.setPort(okAddress.getPort())

        when:
        instance.endpointActivation(factory, spec)

        then:
        instance.server.channel.isActive()
    }

    def "Deactivate endpoint"()
    {
        setup:
        def channel = Mock(Channel)
        channel.close () >> {
            def f = Mock(ChannelFuture)
            f.syncUninterruptibly() >> {
                return f
            }
            return f
        }
        channel.eventLoop() >> {
            def l = Mock(EventLoop)
            l.shutdownGracefully() >> {
                def f = Mock(ChannelFuture)
                f.syncUninterruptibly() >> {
                    return f
                }
                return f
            }
            return l
        }
        def server = new CasualServer(channel)
        instance.server = server

        MessageEndpointFactory factory = Mock(MessageEndpointFactory)
        CasualActivationSpec spec = new CasualActivationSpec()
        spec.setPort(okAddress.getPort())

        when:
        instance.endpointDeactivation( factory, spec )

        then:
        !channel.isOpen()
    }
}
