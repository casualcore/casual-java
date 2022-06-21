package se.laz.casual.network.inbound.reverse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import se.laz.casual.network.CasualNWMessageDecoder;
import se.laz.casual.network.CasualNWMessageEncoder;
import se.laz.casual.network.inbound.CasualMessageHandler;
import se.laz.casual.network.inbound.ExceptionHandler;
import se.laz.casual.network.outbound.EventLoopFactory;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Inbound server that connects and then acts exactly like {@link  se.laz.casual.network.inbound.CasualServer}
 */
public class Server
{
    private static final Logger LOG = Logger.getLogger(Server.class.getName());
    private static final String LOG_HANDLER_NAME = "logHandler";
    private final Channel channel;

    private Server(Channel channel)
    {
        this.channel = channel;
    }

    public static Server of(ReverseInboundConnectionInformation reverseInboundConnectionInformation)
    {
        Objects.requireNonNull(reverseInboundConnectionInformation, "connectionInformation can not be null");
        EventLoopGroup workerGroup = EventLoopFactory.getInstance();
        CasualMessageHandler messageHandler = CasualMessageHandler.of(reverseInboundConnectionInformation.getFactory(), reverseInboundConnectionInformation.getXaTerminator(), reverseInboundConnectionInformation.getWorkManager());
        Channel ch = init(reverseInboundConnectionInformation.getAddress(), workerGroup, messageHandler, ExceptionHandler.of(), reverseInboundConnectionInformation.isLogHandlerEnabled());
        return new Server(ch);
    }

    private static Channel init(final InetSocketAddress address, final EventLoopGroup workerGroup, final CasualMessageHandler messageHandler, ExceptionHandler exceptionHandler, boolean enableLogHandler)
    {
        Bootstrap b = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel ch)
                    {
                        ch.pipeline().addLast(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), messageHandler, exceptionHandler);
                        if(enableLogHandler)
                        {
                            ch.pipeline().addFirst(LOG_HANDLER_NAME, new LoggingHandler(LogLevel.INFO));
                            LOG.info(() -> "reverse inbound log handler enabled");
                        }
                    }
                });
        LOG.info(() -> "reverse inbound about to connect to: " + address);
        return b.connect(address).syncUninterruptibly().channel();
    }

}
