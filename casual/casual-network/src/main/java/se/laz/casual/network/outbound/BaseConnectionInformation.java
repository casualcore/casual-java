/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import se.laz.casual.network.ProtocolVersion;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseConnectionInformation
{
    private final InetSocketAddress address;
    private final UUID domainId;
    private final String domainName;
    private ProtocolVersion protocolVersion;
    private final boolean logHandlerEnabled;
    protected BaseConnectionInformation(final InetSocketAddress address, ProtocolVersion protocolVersion, final UUID domainId, final String domainName, boolean logHandlerEnabled)
    {
        this.address = address;
        this.protocolVersion = protocolVersion;
        this.domainId = domainId;
        this.domainName = domainName;
        this.logHandlerEnabled = logHandlerEnabled;
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
        return protocolVersion.getVersion();
    }

    public boolean isLogHandlerEnabled()
    {
        return logHandlerEnabled;
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
