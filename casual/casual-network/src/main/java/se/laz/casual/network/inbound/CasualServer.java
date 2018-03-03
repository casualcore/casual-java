/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import se.laz.casual.network.CasualNWMessageDecoder;
import se.laz.casual.network.CasualNWMessageEncoder;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * Inbound casual server
 */
public final class CasualServer
{
    private static Logger log = Logger.getLogger(CasualServer.class.getName());
    private static final String USE_LOG_HANDLER_PROPERTY_NAME = "casual.network.inbound.enableLoghandler";
    private final Channel channel;

    public CasualServer(Channel channel)
    {
        this.channel = channel;
    }

    public static CasualServer of(final ConnectionInformation ci)
    {
        CasualMessageHandler mh = CasualMessageHandler.of(ci.getFactory(), ci.getXaTerminator(), ci.getWorkManager());
        Channel c = init(mh, ExceptionHandler.of(), ci.getPort());
        return new CasualServer(c);
    }

    private static Channel init(CasualMessageHandler messageHandler, ExceptionHandler exceptionHandler, int port)
    {
        boolean enableLogHandler = Boolean.parseBoolean(System.getProperty(USE_LOG_HANDLER_PROPERTY_NAME, null));
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap()
            .group(workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                protected void initChannel(SocketChannel ch)
                {
                    ch.pipeline().addLast(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), messageHandler, exceptionHandler);
                    if(enableLogHandler)
                    {
                        ch.pipeline().addFirst("logHandler", new LoggingHandler(LogLevel.INFO));
                    }
                }
            });
        return b.bind(new InetSocketAddress(port)).syncUninterruptibly().channel();
    }

    public void close()
    {
        log.info(() -> "closing server");
        channel.close().syncUninterruptibly();
        channel.eventLoop().shutdownGracefully().syncUninterruptibly();
        log.info(() -> "server closed");
    }

}
