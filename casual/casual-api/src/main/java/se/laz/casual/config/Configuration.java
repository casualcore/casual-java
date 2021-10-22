/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.Objects;
import java.util.Optional;

public class Configuration
{
    private final Inbound inbound;
    private final Domain domain;
    private final Outbound outbound;
    public Configuration(Domain domain, Inbound inbound, Outbound outbound)
    {
        this.domain = domain;
        this.inbound = inbound;
        this.outbound = outbound;
    }

    public Domain getDomain()
    {
        return domain == null ? Domain.of(null) : domain;
    }

    public Inbound getInbound()
    {
        return inbound == null ? Inbound.newBuilder().build() : inbound;
    }

    public Outbound getOutbound()
    {
        return outbound == null ? Outbound.of(Optional.empty(), Optional.empty()) : outbound;
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
        Configuration that = (Configuration) o;
        return Objects.equals(getInbound(), that.getInbound()) && Objects.equals(getDomain(), that.getDomain()) && Objects.equals(getOutbound(), that.getOutbound());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getInbound(), getDomain(), getOutbound());
    }

    @Override
    public String toString()
    {
        return "Configuration{" +
                "inbound=" + getInbound() +
                ", domain=" + getDomain() +
                ", outbound=" + getOutbound() +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private Inbound inbound;
        private Domain domain;
        private Outbound outbound;

        private Builder()
        {
        }

        public Builder withInbound( Inbound inbound )
        {
            this.inbound = inbound;
            return this;
        }

        public Builder withDomain(Domain domain)
        {
            this.domain = domain;
            return this;
        }

        public Builder withOutbound( Outbound outbound )
        {
            this.outbound = outbound;
            return this;
        }

        public Configuration build()
        {
            return new Configuration(domain, inbound, outbound );
        }
    }
}
