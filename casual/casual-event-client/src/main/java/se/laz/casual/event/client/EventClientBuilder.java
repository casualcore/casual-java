/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.client;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Objects;

/**
 * Used to simplify the construction of an EventClient
 */
public final class EventClientBuilder
{
    private EventClientBuilder()
    {}
    public static Builder createBuilder()
    {
        return new Builder();
    }
    public static final class Builder
    {
        private String host;
        private Integer port;
        private Class<? extends Channel> channelClass;
        private EventLoopGroup eventLoopGroup;
        private EventObserver eventObserver;
        private ConnectionObserver connectionObserver;
        private boolean enableLogging;

        private Builder()
        {}
        public Builder withHost(String host)
        {
            this.host = host;
            return this;
        }
        public Builder withPort(Integer port)
        {
            this.port = port;
            return this;
        }
        public Builder withChannel(Class<? extends Channel> channelClass)
        {
            this.channelClass = channelClass;
            return this;
        }
        public Builder withEventLoopGroup(EventLoopGroup eventLoopGroup)
        {
            this.eventLoopGroup = eventLoopGroup;
            return this;
        }
        public Builder withEventObserver(EventObserver eventObserver)
        {
            this.eventObserver = eventObserver;
            return this;
        }

        public Builder withConnectionObserver(ConnectionObserver connectionObserver)
        {
            this.connectionObserver = connectionObserver;
            return this;
        }
        public Builder withEnableLogging(boolean enableLogging)
        {
            this.enableLogging = enableLogging;
            return this;
        }
        public EventClient build()
        {
            Objects.requireNonNull(host, "host can not be null");
            Objects.requireNonNull(port, "port can not be null");
            Objects.requireNonNull(eventObserver, "eventObserver can not be null");
            Objects.requireNonNull(connectionObserver, "connectionObserver can not be null");
            channelClass = null == channelClass ? NioSocketChannel.class : channelClass;
            eventLoopGroup = null == eventLoopGroup ? new NioEventLoopGroup() : eventLoopGroup;
            ConnectionInformation connectionInformation = new ConnectionInformation(host, port);
            EventClientInformation clientInformation = EventClientInformation.createBuilder()
                                                                             .withConnectionInformation(connectionInformation)
                                                                             .withChannelClass(channelClass)
                                                                             .withEventLoopGroup(eventLoopGroup)
                                                                             .build();
            return EventClient.of(clientInformation, eventObserver, connectionObserver, enableLogging);
        }
    }
}
