/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoop
import jakarta.resource.spi.BootstrapContext
import jakarta.resource.spi.XATerminator
import jakarta.resource.spi.endpoint.MessageEndpointFactory
import jakarta.resource.spi.work.WorkException
import jakarta.resource.spi.work.WorkManager
import se.laz.casual.jca.inflow.CasualActivationSpec
import se.laz.casual.network.inbound.CasualServer
import spock.lang.Shared
import spock.lang.Specification

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

    def "Get InboundServerPort property"()
    {
        expect:
        instance.getInboundServerPort() == null
    }

    def "Set InboundServerPort property"()
    {
        given:
        Integer port = 7774

        when:
        instance.setInboundServerPort( port )

        then:
        instance.getInboundServerPort() == port

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
        Integer port = okAddress.getPort()
        instance.setInboundServerPort( port )

        1* manager.startWork( _,_,_,_ ) >> { work, a, b, c ->
            work.run(  )
            return 0
        }

        when:
        instance.endpointActivation(factory, spec)

        then:
        spec.getPort() == port
        instance.getServer(  ).isActive()
    }

    def "Activate endpoint, work fails, InboundStartupException thrown."()
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
        Integer port = okAddress.getPort()
        instance.setInboundServerPort( port )

        1* manager.startWork( _,_,_,_ ) >> { throw new WorkException("Refused") }

        when:
        instance.endpointActivation(factory, spec)

        then:
        thrown InboundStartupException
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
