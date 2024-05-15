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
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import se.laz.casual.event.client.handlers.ConnectionMessageEncoder;
import se.laz.casual.event.client.handlers.ExceptionHandler;
import se.laz.casual.event.client.handlers.FromJSONEventMessageDecoder;
import se.laz.casual.event.client.messages.ConnectionMessage;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Can be used to consume events from casuals EventServer
 *
 * Usage:
 * <pre>
 * EventClient client = EventClient.of(clientInformation, eventObserver, connectionObserver, enableLogging);
 * client.connect();
 * </pre>
 * The eventObserver will be notified regarding any ServiceCallEvents,
 * the connectionObserver will be notified if the connection is closed.
 */
public class EventClient
{
    private static final Logger LOG = Logger.getLogger(EventClient.class.getName());
    private final Channel channel;
    private final CompletableFuture<Boolean> connectFuture;
    private final AtomicBoolean connected = new AtomicBoolean(true);
    private EventClient(Channel channel, CompletableFuture<Boolean> connectFuture)
    {
        this.channel = channel;
        this.connectFuture = connectFuture;
    }
    public static EventClient of(EventClientInformation clientInformation, EventObserver eventObserver, ConnectionObserver connectionObserver, boolean enableLogging)
    {
        Objects.requireNonNull(clientInformation, "clientInformation can not be null");
        Objects.requireNonNull(eventObserver, "eventObserver can not be null");
        Objects.requireNonNull(connectionObserver, "connectionObserver can not be null");
        CompletableFuture<Boolean> connectFuture = new CompletableFuture<>();
        Channel channel = init(clientInformation, eventObserver, connectFuture, enableLogging);
        EventClient client =  new EventClient(channel, connectFuture);
        channel.closeFuture().addListener(f -> client.handleClose(client, connectionObserver));
        return client;
    }

    /**
     * Convenience method to create a client
     * Note: if you do not select the channel class and event loop group - NIO will be chosen for you
     * @return
     */
    public static EventClientBuilder.Builder createBuilder()
    {
        return EventClientBuilder.createBuilder();
    }

    private void handleClose(EventClient client, ConnectionObserver connectionObserver)
    {
        if(client.connected.get())
        {
            connectionObserver.disconnected(this);
        }
    }

    /**
     * @return a future that is completed once the logical connection has been established
     */
    public CompletableFuture<Boolean> connect()
    {
        channel.writeAndFlush(ConnectionMessage.of());
        return connectFuture;
    }
    public void close()
    {
        connected.set(false);
        channel.close();
    }
    private static Channel init(final EventClientInformation clientInformation, EventObserver eventObserver, CompletableFuture<Boolean> connectFuture, boolean enableLogHandler)
    {
        EventLoopGroup workerGroup = clientInformation.getEventLoopGroup();
        Class<? extends Channel> channelClass = clientInformation.getChannelClass();
        Bootstrap b = new Bootstrap()
                .group(workerGroup)
                .channel(channelClass)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel ch)
                    {
                        ch.pipeline().addLast(ConnectionMessageEncoder.of(), new JsonObjectDecoder(), FromJSONEventMessageDecoder.of(eventObserver, connectFuture), ExceptionHandler.of());
                        if(enableLogHandler)
                        {
                            ch.pipeline().addFirst(new LoggingHandler(LogLevel.INFO));
                        }
                    }
                });
        LOG.finest(() -> "about to connect to: " + clientInformation.getConnectionInformation().getAddress());
        return b.connect(clientInformation.getConnectionInformation().getAddress()).syncUninterruptibly().channel();
    }
}
