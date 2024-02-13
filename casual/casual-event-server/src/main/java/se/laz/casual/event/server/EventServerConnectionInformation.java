package se.laz.casual.event.server;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class EventServerConnectionInformation
{
    public static final String USE_LOG_HANDLER_ENV_NAME = "CASUAL_EVENT_SERVER_ENABLE_LOGHANDLER";
    private final int port;
    private final boolean logHandlerEnabled;
    private final boolean useEpoll;
    private final ServerInitialization serverInitialization;

    private EventServerConnectionInformation(Builder builder)
    {
        port = builder.port;
        logHandlerEnabled = builder.logHandlerEnabled;
        useEpoll = builder.useEpoll;
        serverInitialization = builder.serverInitialization;
    }

    public int getPort()
    {
        return port;
    }

    public boolean isLogHandlerEnabled()
    {
        return logHandlerEnabled;
    }

    public boolean isUseEpoll()
    {
        return useEpoll;
    }

    public EventLoopGroup createEventLoopGroup()
    {
        return isUseEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }

    public Class<? extends ServerChannel> getChannelClass()
    {
        return isUseEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public ServerInitialization getServerInitialization()
    {
        return serverInitialization;
    }

    public static final class Builder
    {
        private int port;
        private boolean logHandlerEnabled;
        private boolean useEpoll;
        private ServerInitialization serverInitialization;

        private Builder()
        {}

        public static Builder createBuilder()
        {
            return new Builder();
        }

        public Builder withPort(int port)
        {
            this.port = port;
            return this;
        }

        public Builder withUseEpoll(boolean useEpoll)
        {
            this.useEpoll = useEpoll;
            return this;
        }

        public Builder withServerInitialization(ServerInitialization serverInitialization)
        {
            this.serverInitialization = serverInitialization;
            return this;
        }

        public EventServerConnectionInformation build()
        {
            logHandlerEnabled = Boolean.parseBoolean(System.getenv(USE_LOG_HANDLER_ENV_NAME));
            serverInitialization = null == serverInitialization ? DefaultServerInitialization.of() : serverInitialization;
            return new EventServerConnectionInformation(this);
        }
    }
}
