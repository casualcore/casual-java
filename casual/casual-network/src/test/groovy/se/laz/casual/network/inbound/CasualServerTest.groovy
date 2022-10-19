/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoop
import io.netty.channel.epoll.EpollServerSocketChannel
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage
import se.laz.casual.network.utils.FakeListener
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.XATerminator
import javax.resource.spi.endpoint.MessageEndpointFactory
import javax.resource.spi.work.WorkManager
import java.nio.channels.SocketChannel

class CasualServerTest extends Specification
{
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
        CasualNWMessageImpl<CasualDomainConnectRequestMessage> message = CasualNWMessageImpl.of( correlationId,
                CasualDomainConnectRequestMessage.createBuilder()
                        .withDomainId( domainId )
                        .withDomainName(domainName )
                        .withExecution( execution )
                        .withProtocols(Arrays.asList(protocolVersion))
                        .build()
        )
        def listener = Mock(FakeListener)
        def factory = Mock(MessageEndpointFactory)
        factory.createEndpoint(_) >> {
            return listener
        }
        def xaTerminator = Mock(XATerminator)
        def workManager = Mock(WorkManager)
        server = CasualServer.of(ConnectionInformation.createBuilder()
                                                                  .withPort(0)
                                                                  .withXaTerminator(xaTerminator)
                                                                  .withFactory(factory)
                                                                  .withWorkManager(workManager)
                                                                  .build())
        InetSocketAddress address = (InetSocketAddress) server.channel.localAddress()

        when:
        sendMsg(address, message)

        then:
        server.isActive(  )

        when:
        server.close()

        then:
        noExceptionThrown()
        1 * listener.domainConnectRequest(*_)
        !server.isActive(  )
        !server.channel.isOpen()
    }

    def 'connect and send a message using epoll'()
    {
        setup:
        CasualNWMessageImpl<CasualDomainConnectRequestMessage> message = CasualNWMessageImpl.of( correlationId,
                CasualDomainConnectRequestMessage.createBuilder()
                        .withDomainId( domainId )
                        .withDomainName(domainName )
                        .withExecution( execution )
                        .withProtocols(Arrays.asList(protocolVersion))
                        .build()
        )
        def listener = Mock(FakeListener)
        def factory = Mock(MessageEndpointFactory)
        factory.createEndpoint(_) >> {
            return listener
        }
        def xaTerminator = Mock(XATerminator)
        def workManager = Mock(WorkManager)
        server = CasualServer.of(ConnectionInformation.createBuilder()
                .withPort(0)
                .withXaTerminator(xaTerminator)
                .withFactory(factory)
                .withWorkManager(workManager)
                .withUseEpoll( true )
                .build())
        InetSocketAddress address = (InetSocketAddress) server.channel.localAddress()

        when:
        sendMsg(address, message)

        then:
        server.isActive(  )

        when:
        server.close()

        then:
        noExceptionThrown()
        1 * listener.domainConnectRequest(*_)
        !server.isActive(  )
        !server.channel.isOpen()
    }

    def "Check channel is set."()
    {
        given:
        def listener = Mock(FakeListener)
        def factory = Mock(MessageEndpointFactory)
        factory.createEndpoint(_) >> {
            return listener
        }
        def xaTerminator = Mock(XATerminator)
        def workManager = Mock(WorkManager)

        when:
        server = CasualServer.of(ConnectionInformation.createBuilder()
                .withPort(0)
                .withXaTerminator(xaTerminator)
                .withFactory(factory)
                .withWorkManager(workManager)
                .withUseEpoll( true )
                .build())
        Channel channel = server.channel

        then:
        channel instanceof EpollServerSocketChannel

    }

    def sendMsg(InetSocketAddress address, CasualNWMessageImpl<CasualDomainConnectRequestMessage> msg)
    {
        SocketChannel socketChannel = SocketChannel.open(address)
        CasualMessageEncoder.write(socketChannel, msg)
    }
}