/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound


import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoop
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import se.laz.casual.internal.network.InboundConnectionInformation
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualDomainConnectRequest
import se.laz.casual.network.messages.CasualRequest
import se.laz.casual.network.utils.FakeListener
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.XATerminator
import javax.resource.spi.endpoint.MessageEndpointFactory
import javax.resource.spi.work.WorkManager

class CasualServerTest extends Specification
{
    @Shared EmbeddedChannel embeddedChannel
    @Shared Channel channel
    @Shared CasualServer server
    @Shared UUID correlationId = UUID.randomUUID()
    @Shared UUID domainId = UUID.randomUUID()
    @Shared String domainName = "java"
    @Shared UUID execution = UUID.randomUUID()
    @Shared long protocolVersion = 1000L

    def setup()
    {
        channel = Mock(Channel)
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
        server = new CasualServer(channel)
    }

    def 'close'()
    {
        when:
        server.close()
        then:
        noExceptionThrown()
        !channel.isOpen()
    }

    def 'connect and send a message'()
    {
        setup:
        CasualDomainConnectRequest request = CasualDomainConnectRequest.newBuilder()
                .setDomainId(MessageCreator.toUUID4(domainId))
                .setDomainName(domainName)
                .setExecution(MessageCreator.toUUID4(execution))
                .addProtocolVersion(protocolVersion)
                .build()

        CasualRequest message = CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.DOMAIN_CONNECT_REQUEST)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setDomainConnect(request)
                .build()

        def listener = Mock(FakeListener)
        def factory = Mock(MessageEndpointFactory)
        factory.createEndpoint(_) >> {
            return listener
        }
        def xaTerminator = Mock(XATerminator)
        def workManager = Mock(WorkManager)
        server = CasualServer.of(InboundConnectionInformation.createBuilder()
                                                                  .withPort(0)
                                                                  .withXaTerminator(xaTerminator)
                                                                  .withFactory(factory)
                                                                  .withWorkManager(workManager)
                                                                  .build())
        server.close()
        // Set our own embedded channel to use for the "server"
        server.channel = new EmbeddedChannel(new ProtobufVarint32FrameDecoder(), new ProtobufDecoder(CasualRequest.getDefaultInstance()),
                new ProtobufVarint32LengthFieldPrepender(), new ProtobufEncoder(), CasualMessageHandler.of(factory, xaTerminator, workManager), ExceptionHandler.of())
        when:
        sendMsg(message, server.channel)
        then:
        noExceptionThrown()
        1 * listener.domainConnectRequest(*_)
    }

    def sendMsg(CasualRequest msg, EmbeddedChannel ch)
    {
        ch.writeOneInbound(msg)
    }

}
