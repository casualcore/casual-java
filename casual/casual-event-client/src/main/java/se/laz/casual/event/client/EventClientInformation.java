/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.client;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

import java.util.Objects;

public class EventClientInformation
{
    private final ConnectionInformation connectionInformation;
    private final Class<? extends Channel> channelClass;
    private final EventLoopGroup eventLoopGroup;

    private EventClientInformation(Builder builder)
    {
        connectionInformation = builder.connectionInformation;
        channelClass = builder.channelClass;
        eventLoopGroup = builder.eventLoopGroup;
    }

    public ConnectionInformation getConnectionInformation()
    {
        return connectionInformation;
    }

    public Class<? extends Channel> getChannelClass()
    {
        return channelClass;
    }

    public EventLoopGroup getEventLoopGroup()
    {
        return eventLoopGroup;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private ConnectionInformation connectionInformation;
        private Class<? extends Channel> channelClass;
        private EventLoopGroup eventLoopGroup;

        private Builder()
        {}

        public static Builder newBuilder()
        {
            return new Builder();
        }

        public Builder withConnectionInformation(ConnectionInformation connectionInformation)
        {
            this.connectionInformation = connectionInformation;
            return this;
        }

        /**
         * The channel class should be same family as the event loop group.
         * That is if you use EpollSocketChannel then you should also
         * use EpollEventLoopGroup
         * @param channelClass - the channel class, should be the same family as the event loop group
         * @return The builder
         */
        public Builder withChannelClass(Class<? extends Channel> channelClass)
        {
            this.channelClass = channelClass;
            return this;
        }

        /**
         * The event loop group should be same family as the channel class.
         * That is if you use EpollEventLoopGroup then you should also
         * use EpollSocketChannel
         * @param eventLoopGroup - the event loop group, should be the same family as the channel
         * @return The builder
         */
        public Builder withEventLoopGroup(EventLoopGroup eventLoopGroup)
        {
            this.eventLoopGroup = eventLoopGroup;
            return this;
        }

        public EventClientInformation build()
        {
            Objects.requireNonNull(connectionInformation, "connectionInformation can not be null");
            Objects.requireNonNull(channelClass, "channelClass can not be null");
            Objects.requireNonNull(eventLoopGroup, "eventLoopGroup can not be null");
            return new EventClientInformation(this);
        }
    }
}
