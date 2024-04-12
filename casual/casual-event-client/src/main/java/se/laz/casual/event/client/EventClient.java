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
import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.client.handlers.ConnectionMessageEncoder;
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
    public static EventClient of(ConnectionInformation connectionInformation, EventObserver observer, boolean enableLogging)
    {
        Objects.requireNonNull(connectionInformation, "connectionInformation can not be null");
        Objects.requireNonNull(observer, "observer can not be null");
        Channel channel = init(connectionInformation.getAddress(), observer, enableLogging);
        return new EventClient(channel);
    }
    public void connect()
    {
        channel.writeAndFlush(ConnectionMessage.of());
    }
    public void close()
    {
        channel.close();
    }
    private static Channel init(final InetSocketAddress address, EventObserver observer, boolean enableLogHandler)
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
                        ch.pipeline().addLast(ConnectionMessageEncoder.of(), new JsonObjectDecoder(MAX_MESSAGE_BYTE_SIZE), FromJSONEventMessageDecoder.of(observer));
                        if(enableLogHandler)
                        {
                            ch.pipeline().addFirst(new LoggingHandler(LogLevel.INFO));
                        }
                    }
                });
        LOG.finest(() -> "about to connect to: " + address);
        return b.connect(address).syncUninterruptibly().channel();
    }

    public static void main(String[] args)
    {
        ConnectionInformation connectionInformation = new ConnectionInformation("10.97.100.236", 7698);
        WrapperClient client = WrapperClient.of();
        client.connect(connectionInformation);
        for(;;);
    }

    private static class WrapperClient implements EventObserver
    {
        private EventClient client;
        public static WrapperClient of()
        {
            return new WrapperClient();
        }

        public void connect(ConnectionInformation connectionInformation)
        {
            client = EventClient.of(connectionInformation, this, true);
            client.connect();
        }

        @Override
        public void notify(ServiceCallEvent event)
        {
            System.out.println(event);
        }
    }

}
