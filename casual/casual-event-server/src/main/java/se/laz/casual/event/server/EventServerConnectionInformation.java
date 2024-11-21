/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;

import java.util.Objects;

public class EventServerConnectionInformation
{
    private final int port;
    private final boolean logHandlerEnabled;
    private final boolean useEpoll;

    private final long shutdownQuietPeriod;
    private final long shutdownTimeout;
    private final ServerInitialization serverInitialization;

    private EventServerConnectionInformation(Builder builder)
    {
        this.port = builder.port;
        this.logHandlerEnabled = builder.logHandlerEnabled;
        this.useEpoll = builder.useEpoll;
        this.serverInitialization = builder.serverInitialization;
        this.shutdownTimeout = builder.timeout;
        this.shutdownQuietPeriod = builder.quietPeriod;
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

    public long getShutdownQuietPeriod()
    {
        return shutdownQuietPeriod;
    }

    public long getShutdownTimeout()
    {
        return shutdownTimeout;
    }

    public EventLoopGroup createEventLoopGroup()
    {
        return isUseEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }

    public Class<? extends ServerChannel> getChannelClass()
    {
        return isUseEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        EventServerConnectionInformation that = (EventServerConnectionInformation) o;
        return port == that.port && logHandlerEnabled == that.logHandlerEnabled && useEpoll == that.useEpoll && shutdownQuietPeriod == that.shutdownQuietPeriod && shutdownTimeout == that.shutdownTimeout && Objects.equals( serverInitialization, that.serverInitialization );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( port, logHandlerEnabled, useEpoll, shutdownQuietPeriod, shutdownTimeout, serverInitialization );
    }

    @Override
    public String toString()
    {
        return "EventServerConnectionInformation{" +
                "port=" + port +
                ", logHandlerEnabled=" + logHandlerEnabled +
                ", useEpoll=" + useEpoll +
                ", shutdownQuietPeriod=" + shutdownQuietPeriod +
                ", shutdownTimeout=" + shutdownTimeout +
                ", serverInitialization=" + serverInitialization +
                '}';
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static Builder createBuilder( EventServerConnectionInformation src )
    {
        return new Builder().withPort( src.getPort() )
                .withUseEpoll( src.isUseEpoll() )
                .withServerInitialization( src.getServerInitialization() )
                .withShutdownQuietPeriod( src.getShutdownQuietPeriod() )
                .withShutdownTimeout( src.getShutdownTimeout() );
    }

    public ServerInitialization getServerInitialization()
    {
        return serverInitialization;
    }

    public static final class Builder
    {
        private int port;
        private Boolean logHandlerEnabled;
        private boolean useEpoll;
        private long quietPeriod;
        private long timeout;
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

        public Builder withShutdownQuietPeriod( long period )
        {
            this.quietPeriod = period;
            return this;
        }

        public Builder withShutdownTimeout( long timeout )
        {
            this.timeout = timeout;
            return this;
        }

        public Builder withLogHandlerEnabled( Boolean enabled )
        {
            this.logHandlerEnabled = enabled;
            return this;
        }

        public EventServerConnectionInformation build()
        {
            if( logHandlerEnabled == null )
            {
                logHandlerEnabled = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_EVENT_SERVER_ENABLE_LOGHANDLER );
            }
            serverInitialization = null == serverInitialization ? DefaultServerInitialization.of() : serverInitialization;
            return new EventServerConnectionInformation(this);
        }
    }
}
