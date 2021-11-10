/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.channel.embedded.EmbeddedChannel
import se.laz.casual.network.CasualNWMessageDecoder
import se.laz.casual.network.CasualNWMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage
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
        ch = new EmbeddedChannel(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), CasualMessageHandler.of(correlator), ExceptionHandler.of(correlator, Mock(OnNetworkError)))
        instance = new NettyNetworkConnection(ci, correlator, ch)
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
        def channel = Mock(Channel)
        def channelFuture = Mock(ChannelFuture)
        1 * channel.disconnect() >> {
            channelFuture
        }
        channelFuture.syncUninterruptibly() >> {
            channelFuture
        }
        instance = new NettyNetworkConnection(ci, correlator, channel)
        when:
        instance.close()
        then:
        noExceptionThrown()
        casualDisconnected == false
    }

    def 'casual disconnected'()
    {
        given:
        def channel = new EmbeddedChannel(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), CasualMessageHandler.of(correlator), ExceptionHandler.of(correlator, Mock(OnNetworkError)))
        def newInstance = new NettyNetworkConnection(ci, correlator, channel)
        def future = channel.closeFuture().addListener({ f -> se.laz.casual.network.outbound.NettyNetworkConnection.handleClose(newInstance, this) })
        when:
        future.channel().disconnect()
        then:
        casualDisconnected == true
    }


    def 'exception when reading'()
    {
        given:
        def messageDecoder = Mock(CasualNWMessageDecoder){
            channelRead(_, _) >> { ChannelHandlerContext ctx, Object msg ->
                ctx.fireExceptionCaught(new IOException("boom"))
            }
        }
        def onNetworkError = Mock(OnNetworkError)
        def channel = new EmbeddedChannel(messageDecoder, CasualNWMessageEncoder.of(), CasualMessageHandler.of(correlator), ExceptionHandler.of(correlator, onNetworkError))
        def networkError = false
        onNetworkError.notifyListenerIfNotConnected(channel) >> {
            networkError = true
        }
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> replyMessage = createReplyMessage()
        when:
        channel.writeOneInbound(replyMessage)
        channel.flushInbound()
        then:
        networkError == true
    }

    def 'exception when writing'()
    {
        given:
        def messageEncoder = Mock(CasualNWMessageEncoder){
            write(_, _, _) >> { ChannelHandlerContext ctx, Object msg, ChannelPromise promise ->
                ctx.fireExceptionCaught(new IOException("boom"))
            }
        }
        def onNetworkError = Mock(OnNetworkError)
        def channel = new EmbeddedChannel(CasualNWMessageDecoder.of(), messageEncoder, CasualMessageHandler.of(correlator), ExceptionHandler.of(correlator, onNetworkError))
        def networkError = false
        onNetworkError.notifyListenerIfNotConnected(channel) >> {
            networkError = true
        }
        def localInstance = new NettyNetworkConnection(ci, correlator, channel)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> requestMessage = createRequestMessage()
        when:
        localInstance.request(requestMessage)
        then:
        networkError == true
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

    def createReplyMessage()
    {
        CasualDomainDiscoveryReplyMessage message = CasualDomainDiscoveryReplyMessage.of(UUID.randomUUID(), UUID.randomUUID(), 'test-domain')
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
