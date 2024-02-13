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
import se.laz.casual.event.server.handlers.EventMessageEncoder;
import se.laz.casual.event.server.handlers.ExceptionHandler;
import se.laz.casual.event.server.handlers.FromJSONLogonDecoder;

import java.net.InetSocketAddress;
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
    public Channel init(EventServerConnectionInformation connectionInformation, ChannelGroup connectedClients)
    {
        EventLoopGroup bossGroup = connectionInformation.createEventLoopGroup();
        EventLoopGroup workerGroup = connectionInformation.createEventLoopGroup();
        Class<? extends ServerChannel> channelClass = connectionInformation.getChannelClass();
        ServerBootstrap b = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(channelClass)
                .childHandler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel ch)
                    {
                        ch.pipeline().addLast(new JsonObjectDecoder(MAX_LOGON_PAYLOAD_SIZE), FromJSONLogonDecoder.of(connectedClients), EventMessageEncoder.of(), ExceptionHandler.of(connectedClients));
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
