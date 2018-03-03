/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import se.laz.casual.internal.jca.ManagedConnectionInvalidator;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

public final class NettyConnectionInformation extends BaseConnectionInformation
{
    private final ManagedConnectionInvalidator invalidator;
    private final Class<? extends Channel> channelClass;
    private final Correlator correlator;
    private NettyConnectionInformation(InetSocketAddress address, long protocolVersion, UUID domainId, String domainName, ManagedConnectionInvalidator invalidator, Class<? extends Channel> channelClass, Correlator correlator)
    {
        super(address, protocolVersion, domainId, domainName);
        this.invalidator = invalidator;
        this.channelClass = channelClass;
        this.correlator = correlator;
    }

    public ManagedConnectionInvalidator getInvalidator()
    {
        return invalidator;
    }

    public Class<? extends Channel> getChannelClass()
    {
        return channelClass;
    }

    public Correlator getCorrelator()
    {
        return correlator;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private InetSocketAddress address;
        private UUID domainId;
        private ManagedConnectionInvalidator invalidator;
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

        public Builder withInvalidator(ManagedConnectionInvalidator invalidator)
        {
            this.invalidator = invalidator;
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
            Objects.requireNonNull(invalidator, "invalidator can not be null");
            channelClass = (null == channelClass) ? NioSocketChannel.class : channelClass;
            correlator = (null == correlator) ? CorrelatorImpl.of() : correlator;
            return new NettyConnectionInformation(address, protocolVersion, domainId, domainName, invalidator, channelClass, correlator);
        }
    }
}
