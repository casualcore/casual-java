/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import io.netty.channel.EventLoopGroup
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.concurrent.Future
import se.laz.casual.network.CasualNWMessageDecoder
import se.laz.casual.network.CasualNWMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class NettyNetworkConnectionTest extends Specification implements NetworkListener
{
    @Shared UUID corrid = UUID.randomUUID()
    @Shared NettyNetworkConnection instance
    @Shared NettyConnectionInformation ci
    @Shared Correlator correlator
    @Shared EmbeddedChannel ch
    private boolean casualDisconnected = false;

    def setup()
    {
        correlator = CorrelatorImpl.of()
        ci = NettyConnectionInformation.createBuilder()
                                                            .withAddress(new InetSocketAddress(3712))
                                                            .withProtocolVersion(1000l)
                                                            .withDomainId(UUID.randomUUID())
                                                            .withDomainName('testDomain')
                                                            .withCorrelator(correlator)
                                                            .build()
        ch = new EmbeddedChannel(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), CasualMessageHandler.of(correlator), ExceptionHandler.of(correlator))
        instance = new NettyNetworkConnection(ci, correlator, ch, null)
    }

    def 'Of with a null connection info throws NullPointerException.'()
    {
        when:
        NettyNetworkConnection.of(connectionInformation, networkListener)
        then:
        thrown NullPointerException
        where:
        connectionInformation            | networkListener
        null                             | Mock(NetworkListener)
        ci                               | null
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
            1 * nettyFuture.addListener(_) >> {
                return nettyFuture
            }
            return nettyFuture
        }
        instance = new NettyNetworkConnection(ci, correlator, ch, eventloopGroup)
        when:
        instance.close()
        then:
        noExceptionThrown()
        casualDisconnected == false
    }

    def 'casual disconnected'()
    {
        given:
        def channel = new EmbeddedChannel(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), CasualMessageHandler.of(correlator), ExceptionHandler.of(correlator))
        def newInstance = new NettyNetworkConnection(ci, correlator, channel, null)
        def future = channel.closeFuture().addListener({ f -> se.laz.casual.network.outbound.NettyNetworkConnection.handleClose(newInstance, this) })
        when:
        future.channel().disconnect()
        then:
        casualDisconnected == true
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

    @Override
    void disconnected()
    {
        casualDisconnected = true;
    }
}
