/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

public final class NettyConnectionInformation extends BaseConnectionInformation
{
    private final Class<? extends Channel> channelClass;
    private final Correlator correlator;
    private NettyConnectionInformation(InetSocketAddress address, long protocolVersion, UUID domainId, String domainName, Class<? extends Channel> channelClass, Correlator correlator)
    {
        super(address, protocolVersion, domainId, domainName);
        this.channelClass = channelClass;
        this.correlator = correlator;
    }

    public Class<? extends Channel> getChannelClass()
    {
        return channelClass;
    }

    public Correlator getCorrelator()
    {
        return correlator;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }
        NettyConnectionInformation that = (NettyConnectionInformation) o;
        return Objects.equals(channelClass, that.channelClass) &&
                Objects.equals(correlator, that.correlator);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), channelClass, correlator);
    }

    @Override
    public String toString()
    {
        return "NettyConnectionInformation{" +
                "channelClass=" + channelClass +
                ", correlator=" + correlator +
                '}';
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private InetSocketAddress address;
        private UUID domainId;
        private String domainName;
        private long protocolVersion;
        private Class<? extends Channel> channelClass;
        private Correlator correlator;

        public Builder withAddress(InetSocketAddress address)
        {
            this.address = address;
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

        public Builder withProtocolVersion(long protocolVersion)
        {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder withChannelClass(Class<? extends Channel> channelClass)
        {
            this.channelClass = channelClass;
            return this;
        }

        public Builder withCorrelator(Correlator correlator)
        {
            this.correlator = correlator;
            return this;
        }

        public NettyConnectionInformation build()
        {
            Objects.requireNonNull(address, "address can not be null");
            Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
            Objects.requireNonNull(domainId, "domainId can not be null");
            Objects.requireNonNull(domainName, "domainName can not be null");
            channelClass = (null == channelClass) ? NioSocketChannel.class : channelClass;
            correlator = (null == correlator) ? CorrelatorImpl.of() : correlator;
            return new NettyConnectionInformation(address, protocolVersion, domainId, domainName, channelClass, correlator);
        }
    }
}
