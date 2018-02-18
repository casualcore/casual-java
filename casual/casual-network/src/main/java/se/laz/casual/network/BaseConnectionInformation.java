package se.laz.casual.network;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseConnectionInformation
{
    private final InetSocketAddress address;
    private final UUID domainId;
    private final String domainName;
    private long protocolVersion;
    protected BaseConnectionInformation(final InetSocketAddress address, long protocolVersion, final UUID domainId, final String domainName)
    {
        this.address = address;
        this.protocolVersion = protocolVersion;
        this.domainId = domainId;
        this.domainName = domainName;
    }

    public InetSocketAddress getAddress()
    {
        return new InetSocketAddress(address.getAddress(), address.getPort());
    }

    public UUID getDomainId()
    {
        return domainId;
    }

    public String getDomainName()
    {
        return domainName;
    }

    public long getProtocolVersion()
    {
        return protocolVersion;
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
        BaseConnectionInformation that = (BaseConnectionInformation) o;
        return protocolVersion == that.protocolVersion &&
            Objects.equals(address, that.address) &&
            Objects.equals(domainId, that.domainId) &&
            Objects.equals(domainName, that.domainName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(address, domainId, domainName, protocolVersion);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("BaseConnectionInformation{");
        sb.append("address=").append(address);
        sb.append(", domainId=").append(domainId);
        sb.append(", domainName='").append(domainName).append('\'');
        sb.append(", protocolVersion=").append(protocolVersion);
        sb.append('}');
        return sb.toString();
    }
}
