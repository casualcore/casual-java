package se.laz.casual.network.inbound.reverse;

import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.outbound.Correlator;
import se.laz.casual.network.outbound.CorrelatorImpl;

import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
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

    private ReverseInboundConnectionInformation(InetSocketAddress address, ProtocolVersion protocolVersion, Correlator correlator, MessageEndpointFactory factory, XATerminator xaTerminator, WorkManager workManager, boolean logHandlerEnabled, UUID domainId, String domainName)
    {
        this.address = address;
        this.protocolVersion = protocolVersion;
        this.correlator = correlator;
        this.factory = factory;
        this.xaTerminator = xaTerminator;
        this.workManager = workManager;
        this.logHandlerEnabled = logHandlerEnabled;
        this.domainId = domainId;
        this.domainName = domainName;
    }

    public InetSocketAddress getAddress()
    {
        return address;
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
            return new ReverseInboundConnectionInformation(address, protocolVersion, correlator, factory, xaTerminator, workManager, Boolean.parseBoolean(System.getenv(USE_LOG_HANDLER_ENV_NAME)), domainId, domainName);
        }
    }
}
