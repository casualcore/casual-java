/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.reverse.outbound;

import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.network.outbound.NettyNetworkConnection;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public final class ReverseOutboundConnectionInformation
{
    private final String name;
    private final int port;
    private final UUID domainId;
    private final String domainName;
    private final Class<? extends ServerChannel> channelClass;
    private final Consumer<NettyNetworkConnection> connectionConsumer;
    private final boolean useEpoll;

    private ReverseOutboundConnectionInformation(Builder builder)
    {
        this.name = builder.name;
        this.port = builder.port;
        this.domainId = builder.domainId;
        this.domainName = builder.domainName;
        this.channelClass = builder.channelClass;
        this.connectionConsumer = builder.connectionConsumer;
        this.useEpoll = builder.useEpoll;
    }

    public String getName()
    {
        return name;
    }

    public int getPort()
    {
        return port;
    }

    public UUID getDomainId()
    {
        return domainId;
    }

    public String getDomainName()
    {
        return domainName;
    }

    public Class<? extends ServerChannel> getChannelClass()
    {
        return channelClass;
    }

    public boolean isUseEpoll()
    {
        return useEpoll;
    }

    /**
     * Consumer of established connections, invoked after the handshake and role switch completes.
     */
    public Consumer<NettyNetworkConnection> getConnectionConsumer()
    {
        return connectionConsumer;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private String name;
        private int port;
        private UUID domainId;
        private String domainName;
        private Class<? extends ServerChannel> channelClass;
        private Consumer<NettyNetworkConnection> connectionConsumer;
        private boolean useEpoll;

        public Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder withPort(int port)
        {
            this.port = port;
            return this;
        }

        public Builder withDomainId(UUID domainId)
        {
            this.domainId = domainId;
            return this;
        }

        public Builder withDomainName(String domainName)
        {
            this.domainName = domainName;
            return this;
        }

        public Builder withUseEpoll(boolean useEpoll)
        {
            this.useEpoll = useEpoll;
            this.channelClass = useEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
            return this;
        }

        public Builder withConnectionConsumer(Consumer<NettyNetworkConnection> connectionConsumer)
        {
            this.connectionConsumer = connectionConsumer;
            return this;
        }

        public ReverseOutboundConnectionInformation build()
        {
            Objects.requireNonNull(name, "name can not be null");
            Objects.requireNonNull(domainId, "domainId can not be null");
            Objects.requireNonNull(domainName, "domainName can not be null");
            Objects.requireNonNull(connectionConsumer, "connectionConsumer can not be null");
            if( channelClass == null )
            {
                useEpoll = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL );
                channelClass = useEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
            }
            return new ReverseOutboundConnectionInformation( this );
        }
    }
}
