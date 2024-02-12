package se.laz.casual.event.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import se.laz.casual.event.Order;
import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.ServiceCallEventHandler;
import se.laz.casual.event.ServiceCallEventHandlerFactory;
import se.laz.casual.event.ServiceCallEventImpl;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class EventServer
{
    private static final String LOG_HANDLER_NAME = "logHandler";
    private static final Logger log = Logger.getLogger(EventServer.class.getName());
    private static final int MAX_LOGON_PAYLOAD_SIZE = 128;
    private final ChannelGroup  connectedClients;
    private final Channel channel;
    private final ExecutorService executorService;
    private final ServiceCallEventHandler serviceCallEventHandler;

    public EventServer(Channel channel, ExecutorService executorService, ChannelGroup connectedClients)
    {
        Objects.requireNonNull(channel, "channel can not be null");
        Objects.requireNonNull(executorService, "executorService can not be null");
        this.channel = channel;
        this.executorService = executorService;
        this.connectedClients = connectedClients;
        serviceCallEventHandler = ServiceCallEventHandlerFactory.getHandler();
        executorService.execute(this::handleMessages);
    }

    public static EventServer of(EventServerConnectionInformation connectionInformation)
    {
        Objects.requireNonNull(connectionInformation);
        ChannelGroup connectedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        Channel ch = init(connectionInformation, connectedClients);
        return new EventServer(ch, Executors.newSingleThreadExecutor(), connectedClients);
    }

    public boolean isActive()
    {
        if (this.channel != null)
        {
            return this.channel.isActive();
        }
        return false;
    }

    public void close()
    {
        log.info(() -> "closing event server");
        channel.close().syncUninterruptibly();
        channel.eventLoop().shutdownGracefully().syncUninterruptibly();
        log.info(() -> "event server closed");
    }

    private static Channel init(EventServerConnectionInformation connectionInformation, ChannelGroup connectedClients)
    {
        EventLoopGroup bossGroup = connectionInformation.isUseEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        EventLoopGroup workerGroup = connectionInformation.isUseEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        Class<? extends ServerChannel> channelClass = connectionInformation.isUseEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
        ServerBootstrap b = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(channelClass)
                .childHandler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel ch)
                    {
                        //ch.pipeline().addLast(LogonEventMessageDecoder.of(connectedClients), EventMessageEncoder.of(), ExceptionHandler.of(connectedClients));
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

    private void handleMessages()
    {
        while (isActive())
        {
            ServiceCallEvent event = serviceCallEventHandler.take();
            log.info(() -> "# of clients: " + connectedClients.size());
            for (Channel client : connectedClients)
            {
                log.info(() -> "writing to client");
                client.writeAndFlush(event);
            }
        }
    }

    /**
     * Test server that keeps posting test data to any potential clients, discards events if no clients
     * Usage:
     * telnet localhost port# ( default 7689)
     * {"message":"HELLO"}
     * @param args
     */
    public static void main(String[] args)
    {
        List<String> arguments = Arrays.asList(args);
        int port = arguments.stream()
                            .map(value -> Integer.valueOf(value))
                            .findFirst()
                            .orElseGet(() -> 7689);
        System.out.println("EventServer listening to port# " + port);
        EventServerConnectionInformation connectionInformation = EventServerConnectionInformation.createBuilder()
                                                                                                 .withPort(port)
                                                                                                 .withUseEpoll(true)
                                                                                                 .build();
        EventServer.of(connectionInformation);
        Executors.newSingleThreadExecutor().execute(() -> postTestData());
    }

    private static void postTestData()
    {
        while(true)
        {
            ServiceCallEventHandlerFactory.getHandler().put(ServiceCallEventImpl.createBuilder()
                                                                                .withExecution(UUID.randomUUID())
                                                                                .withCode(42)
                                                                                .withOrder(Order.SEQUENTIAL)
                                                                                .withParent("Gigi")
                                                                                .withExecution(UUID.randomUUID())
                                                                                .build());
            try
            {
                Thread.sleep(10 * 1000);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

}
