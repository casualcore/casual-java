/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import io.netty.channel.EventLoopGroup
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.concurrent.Future
import se.laz.casual.internal.jca.ManagedConnectionInvalidator
import se.laz.casual.network.CasualNWMessageDecoder
import se.laz.casual.network.CasualNWMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class NettyNetworkConnectionTest extends Specification
{
    @Shared UUID corrid = UUID.randomUUID()
    @Shared NettyNetworkConnection instance
    @Shared ManagedConnectionInvalidator invalidator
    @Shared NettyConnectionInformation ci
    @Shared Correlator correlator
    @Shared EmbeddedChannel ch

    def setup()
    {
        correlator = CorrelatorImpl.of()
        invalidator = Mock(ManagedConnectionInvalidator)
        ci = NettyConnectionInformation.createBuilder()
                                                            .withAddress(new InetSocketAddress(3712))
                                                            .withProtocolVersion(1000l)
                                                            .withDomainId(UUID.randomUUID())
                                                            .withDomainName('testDomain')
                                                            .withInvalidator(invalidator)
                                                            .withCorrelator(correlator)
                                                            .build()
        ch = new EmbeddedChannel(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), CasualMessageHandler.of(correlator), ExceptionHandler.of(ci.getInvalidator(), correlator))
        instance = new NettyNetworkConnection(ci, ci.getInvalidator(), correlator, ch, null)
    }

    def 'Of with a null connection info throws NullPointerException.'()
    {
        when:
        NettyNetworkConnection.of(null)
        then:
        thrown NullPointerException
    }

    def 'ping ponging a domain discovery request message'()
    {
        setup:
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> m = createRequestMessage()
        when:
        CompletableFuture<CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage>> f = instance.request(m)
        ch.writeOneInbound(m)
        CasualNWMessageImpl<CasualServiceCallReplyMessage> reply = f.get()
        then:
        noExceptionThrown()
        ch.outboundMessages().size() == 1
        reply == m
    }

    def 'close'()
    {
        setup:
        def eventloopGroup = Mock(EventLoopGroup)
        1 * eventloopGroup.shutdownGracefully() >> {
            def nettyFuture = Mock(Future)
            1 * nettyFuture.syncUninterruptibly() >> {
                return nettyFuture
            }
            return nettyFuture
        }
        instance = new NettyNetworkConnection(ci, ci.getInvalidator(), correlator, ch, eventloopGroup)
        when:
        instance.close()
        then:
        noExceptionThrown()
    }

    def createRequestMessage()
    {
        CasualDomainDiscoveryRequestMessage message = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setDomainId(UUID.randomUUID())
                .setDomainName( "test-domain" )
                .setServiceNames(Arrays.asList("echo"))
                .build()
        return CasualNWMessageImpl.of(corrid, message)
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains 'NettyNetworkConnection'
    }

}
