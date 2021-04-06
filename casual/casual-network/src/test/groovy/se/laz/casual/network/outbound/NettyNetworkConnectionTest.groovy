/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import io.netty.channel.EventLoopGroup
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import io.netty.util.concurrent.Future
import org.junit.Ignore
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest
import se.laz.casual.network.messages.CasualReply
import se.laz.casual.network.messages.CasualRequest
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
        ch = new EmbeddedChannel(new ProtobufVarint32FrameDecoder(), new ProtobufDecoder(CasualReply.getDefaultInstance()),
                new ProtobufVarint32LengthFieldPrepender(), new ProtobufEncoder(), CasualProtoBufMessageHandler.of(correlator), ExceptionHandler.of(correlator))
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
        CasualRequest m = createRequestMessage()
        when:
        CompletableFuture<CasualReply> f = instance.request(m)
        ch.writeOneInbound(m)
        CasualReply reply = f.get()
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
        CasualDomainDiscoveryRequest message = CasualDomainDiscoveryRequest.newBuilder()
                .setExecution(MessageCreator.toUUID(UUID.randomUUID()))
                .setDomainId(MessageCreator.toUUID(UUID.randomUUID()))
                .setDomainName( "test-domain" )
                .setServiceNames(0, 'echo')
                .build()
        return CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.DOMAIN_DISCOVERY_REQUEST)
                .setCorrelationId(MessageCreator.toUUID(corrid))
                .setDomainDiscovery(message)
                .build()
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
