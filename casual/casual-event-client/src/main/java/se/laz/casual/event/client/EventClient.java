/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import se.laz.casual.event.client.handlers.ConnectionMessageEncoder;
import se.laz.casual.event.client.handlers.ExceptionHandler;
import se.laz.casual.event.client.handlers.FromJSONEventMessageDecoder;
import se.laz.casual.event.client.messages.ConnectionMessage;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.logging.Logger;

public class EventClient
{
    private static final Logger LOG = Logger.getLogger(EventClient.class.getName());
    private static final int MAX_MESSAGE_BYTE_SIZE = 4096;
    private final Channel channel;
    private EventClient(Channel channel)
    {
        this.channel = channel;
    }
    public static EventClient of(ConnectionInformation connectionInformation, EventObserver eventObserver, ConnectionObserver connectionObserver, boolean enableLogging)
    {
        Objects.requireNonNull(connectionInformation, "connectionInformation can not be null");
        Objects.requireNonNull(eventObserver, "eventObserver can not be null");
        Objects.requireNonNull(connectionObserver, "connectionObserver can not be null");
        Channel channel = init(connectionInformation.getAddress(), eventObserver, enableLogging);
        EventClient client =  new EventClient(channel);
        channel.closeFuture().addListener(f -> handleClose(connectionObserver));
        return client;
    }

    private static void handleClose(ConnectionObserver connectionObserver)
    {
        connectionObserver.connectionClosed();
    }

    public void connect()
    {
        channel.writeAndFlush(ConnectionMessage.of());
    }
    public void close()
    {
        channel.close();
    }
    private static Channel init(final InetSocketAddress address, EventObserver eventObserver, boolean enableLogHandler)
    {
        EventLoopGroup workerGroup = new EpollEventLoopGroup();
        Class<? extends Channel> channelClass = EpollSocketChannel.class;
        Bootstrap b = new Bootstrap()
                .group(workerGroup)
                .channel(channelClass)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel ch)
                    {
                        ch.pipeline().addLast(ConnectionMessageEncoder.of(), new JsonObjectDecoder(MAX_MESSAGE_BYTE_SIZE), FromJSONEventMessageDecoder.of(eventObserver), ExceptionHandler.of());
                        if(enableLogHandler)
                        {
                            ch.pipeline().addFirst(new LoggingHandler(LogLevel.INFO));
                        }
                    }
                });
        LOG.finest(() -> "about to connect to: " + address);
        return b.connect(address).syncUninterruptibly().channel();
    }
}
