/*
 * Copyright (c) 2022 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound.reverse;

import io.netty.channel.Channel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.work.WorkManager;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.outbound.Correlator;
import se.laz.casual.network.outbound.CorrelatorImpl;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

public class ReverseInboundConnectionInformation
{
    public static final String USE_LOG_HANDLER_ENV_NAME = "CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER";
    private final InetSocketAddress address;
    private final ProtocolVersion protocolVersion;
    private final Correlator correlator;
    private final MessageEndpointFactory factory;
    private final XATerminator xaTerminator;
    private final WorkManager workManager;
    private final boolean logHandlerEnabled;
    private final UUID domainId;
    private final String domainName;
    private final Class<? extends Channel> channelClass;
    private boolean useEpoll;
    private final long maxBackoffMillis;

    private ReverseInboundConnectionInformation(Builder builder)
    {
        this.address = builder.address;
        this.protocolVersion = builder.protocolVersion;
        this.correlator = builder.correlator;
        this.factory = builder.factory;
        this.xaTerminator = builder.xaTerminator;
        this.workManager = builder.workManager;
        this.logHandlerEnabled = builder.logHandlerEnabled;
        this.domainId = builder.domainId;
        this.domainName = builder.domainName;
        this.channelClass = builder.channelClass;
        this.useEpoll = builder.useEpoll;
        this.maxBackoffMillis = builder.maxBackoffMillis;
    }

    public InetSocketAddress getAddress()
    {
        // make sure that we always create a new InetSocketAddress in case
        // the ip has changed, thus it has to be resolved again
        return new InetSocketAddress(address.getHostName(), address.getPort());
    }

    public ProtocolVersion getProtocolVersion()
    {
        return protocolVersion;
    }

    public Correlator getCorrelator()
    {
        return correlator;
    }

    public MessageEndpointFactory getFactory()
    {
        return factory;
    }

    public XATerminator getXaTerminator()
    {
        return xaTerminator;
    }

    public WorkManager getWorkManager()
    {
        return workManager;
    }

    public boolean isLogHandlerEnabled()
    {
        return logHandlerEnabled;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public UUID getDomainId()
    {
        return domainId;
    }

    public String getDomainName()
    {
        return domainName;
    }

    public Class<? extends Channel> getChannelClass()
    {
        return channelClass;
    }

    public boolean isUseEpoll()
    {
        return useEpoll;
    }

    public long getMaxBackoffMillis()
    {
        return maxBackoffMillis;
    }

    public static final class Builder
    {
        private InetSocketAddress address;
        private ProtocolVersion protocolVersion;
        private Correlator correlator;
        private MessageEndpointFactory factory;
        private XATerminator xaTerminator;
        private WorkManager workManager;
        private UUID domainId;
        private String domainName;
        private Class<? extends Channel> channelClass;
        private boolean logHandlerEnabled;
        private boolean useEpoll;
        private long maxBackoffMillis;

        public Builder withAddress(InetSocketAddress address)
        {
            this.address = address;
            return this;
        }

        public Builder withProtocolVersion(ProtocolVersion protocolVersion)
        {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder withCorrelator(Correlator correlator)
        {
            this.correlator = correlator;
            return this;
        }

        public Builder withFactory(MessageEndpointFactory factory)
        {
            this.factory = factory;
            return this;
        }

        public Builder withXaTerminator(XATerminator xaTerminator)
        {
            this.xaTerminator = xaTerminator;
            return this;
        }

        public Builder withWorkManager(WorkManager workManager)
        {
            this.workManager = workManager;
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
            return this;
        }

        public Builder withMaxBackoffMillils(long maxBackoffMillis)
        {
            this.maxBackoffMillis = maxBackoffMillis;
            return this;
        }

        public ReverseInboundConnectionInformation build()
        {
            Objects.requireNonNull(address, "address can not be null");
            Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
            Objects.requireNonNull(factory, "factory can not be null");
            Objects.requireNonNull(xaTerminator, "xaTerminator can not be null");
            Objects.requireNonNull(workManager, "workManager can not be null");
            Objects.requireNonNull(domainId, "domainId can not be null");
            Objects.requireNonNull(domainName, "domainName can not be null");
            correlator = null == correlator ? CorrelatorImpl.of() : correlator;
            channelClass = useEpoll ? EpollSocketChannel.class : NioSocketChannel.class;
            logHandlerEnabled = Boolean.parseBoolean(System.getenv(USE_LOG_HANDLER_ENV_NAME));
            return new ReverseInboundConnectionInformation(this);
        }
    }
}
