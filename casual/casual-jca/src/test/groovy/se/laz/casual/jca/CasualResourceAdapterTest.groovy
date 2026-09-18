/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import se.laz.casual.jca.pool.NetworkPoolHandler
import se.laz.casual.config.ConfigurationOptions
import se.laz.casual.config.ConfigurationService
import se.laz.casual.config.ReverseOutbound
import se.laz.casual.jca.work.StartReverseOutboundServerListener
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoop
import jakarta.resource.spi.BootstrapContext
import jakarta.resource.spi.XATerminator
import jakarta.resource.spi.endpoint.MessageEndpointFactory
import jakarta.resource.spi.work.WorkException
import jakarta.resource.spi.work.WorkManager
import se.laz.casual.jca.inflow.CasualActivationSpec
import se.laz.casual.jca.inflow.CasualInboundTransactionRegistry
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

    def cleanup()
    {
       RuntimeInformation.setDomainIsBeingShutdown(false)
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

        CasualInboundTransactionRegistry inboundTransactionRegistry = new CasualInboundTransactionRegistry()
        instance.inboundTransactionRegistry = inboundTransactionRegistry

        MessageEndpointFactory factory = Mock(MessageEndpointFactory)
        CasualActivationSpec spec = new CasualActivationSpec()
        spec.setPort(okAddress.getPort())

        when:
        instance.endpointDeactivation( factory, spec )

        then:
        !channel.isOpen()
        RuntimeInformation.isDomainBeingShutdown()
    }
    def 'activation registers empty reverse pools before listener work and skips duplicate names'()
    {
        given:
        String firstName = "first-${UUID.randomUUID()}"
        String secondName = "second-${UUID.randomUUID()}"
        def handler = NetworkPoolHandler.getInstance()
        def option = ConfigurationOptions.CASUAL_REVERSE_OUTBOUND_INSTANCES
        def previous = ConfigurationService.getConfiguration(option)
        def reverse = { String name, int port ->
            ReverseOutbound.newBuilder().withName(name).withPort(port).build()
        }
        ConfigurationService.setConfiguration(option,
                [reverse(firstName, 7785), reverse(firstName, 7786), reverse(secondName, 7787)])
        WorkManager manager = Mock()
        BootstrapContext context = Mock() {
            getWorkManager() >> manager
            getXATerminator() >> Mock(XATerminator)
        }
        instance.start(context)
        instance.setInboundServerPort(9999)
        def registeredPoolCounts = []

        when:
        instance.endpointActivation(Mock(MessageEndpointFactory), new CasualActivationSpec())

        then:
        2 * manager.startWork(_, _, _, _ as StartReverseOutboundServerListener) >> {
            work, timeout, executionContext, listener ->
                def pools = [firstName, secondName].collect { handler.getPool(it) }.findAll { it != null }
                assert pools.every { it.isReverse() && it.getPoolDomainIds().isEmpty() }
                registeredPoolCounts.add(pools.size())
                return 0L
        }
        // The first work submission sees only firstName. Its duplicate is skipped,
        // the second submission sees both firstName and secondName.
        registeredPoolCounts == [1, 2]
        handler.getPool(firstName).isReverse()
        handler.getPool(secondName).isReverse()
        handler.getPool(firstName).getPoolDomainIds().isEmpty()
        handler.getPool(secondName).getPoolDomainIds().isEmpty()

        cleanup:
        ConfigurationService.setConfiguration(option, previous)
        handler.@pools.remove(firstName)
        handler.@pools.remove(secondName)
    }

}
