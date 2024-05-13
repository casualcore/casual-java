/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.logging.LoggingHandler;
import se.laz.casual.event.server.handlers.ConnectReplyMessageEncoder;
import se.laz.casual.event.server.handlers.EventMessageEncoder;
import se.laz.casual.event.server.handlers.ExceptionHandler;
import se.laz.casual.event.server.handlers.FromJSONConnectDecoder;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.logging.Logger;

public class DefaultServerInitialization implements ServerInitialization
{
    private static final Logger log = Logger.getLogger(DefaultServerInitialization.class.getName());
    private static final String LOG_HANDLER_NAME = "logHandler";
    private static final int MAX_LOGON_PAYLOAD_SIZE = 128;

    private DefaultServerInitialization()
    {}

    public static ServerInitialization of()
    {
        return new DefaultServerInitialization();
    }

    @Override
    public Channel init(EventServerConnectionInformation connectionInformation, ChannelGroup connectedClients, EventLoopGroup bossGroup, EventLoopGroup workerGroup)
    {
        Objects.requireNonNull(connectionInformation, "connectionInformation can not be null");
        Objects.requireNonNull(connectedClients, "connectedClients can not be null");
        Class<? extends ServerChannel> channelClass = connectionInformation.getChannelClass();
        ServerBootstrap b = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(channelClass)
                .childHandler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel ch)
                    {
                        ch.pipeline().addLast(new JsonObjectDecoder(MAX_LOGON_PAYLOAD_SIZE), FromJSONConnectDecoder.of(connectedClients), ConnectReplyMessageEncoder.of() , EventMessageEncoder.of(), ExceptionHandler.of(connectedClients));
                        if (connectionInformation.isLogHandlerEnabled())
                        {
                            ch.pipeline().addFirst(LOG_HANDLER_NAME, new LoggingHandler());
                            log.info(() -> "EventServer network log handler enabled");
                        }
                    }
                }).childOption(ChannelOption.SO_KEEPALIVE, true);
        return b.bind(new InetSocketAddress(connectionInformation.getPort())).syncUninterruptibly().channel();
    }
}
