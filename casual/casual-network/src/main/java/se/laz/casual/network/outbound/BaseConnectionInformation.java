/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseConnectionInformation
{
    private final InetSocketAddress address;
    private final UUID domainId;
    private final String domainName;
    private final boolean logHandlerEnabled;
    protected BaseConnectionInformation(final InetSocketAddress address, final UUID domainId, final String domainName, boolean logHandlerEnabled)
    {
        this.address = address;
        this.domainId = domainId;
        this.domainName = domainName;
        this.logHandlerEnabled = logHandlerEnabled;
    }

    public InetSocketAddress getAddress()
    {
        return InetSocketAddress.createUnresolved(address.getHostName(), address.getPort());
    }

    public UUID getDomainId()
    {
        return domainId;
    }

    public String getDomainName()
    {
        return domainName;
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
        return Objects.equals(address, that.address) &&
            Objects.equals(domainId, that.domainId) &&
            Objects.equals(domainName, that.domainName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(address, domainId, domainName);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("BaseConnectionInformation{");
        sb.append("address=").append(address);
        sb.append(", domainId=").append(domainId);
        sb.append(", domainName='").append(domainName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
