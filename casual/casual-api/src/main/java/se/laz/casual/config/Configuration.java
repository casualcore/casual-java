/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Configuration
{
    private final Inbound inbound;
    private final Domain domain;
    private final Outbound outbound;
    private final List<ReverseInbound> reverseInbound;
    public Configuration(Domain domain, Inbound inbound, Outbound outbound, List<ReverseInbound> reverseInbound)
    {
        this.domain = domain;
        this.inbound = inbound;
        this.outbound = outbound;
        this.reverseInbound = reverseInbound;
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
        return outbound == null ? Outbound.newBuilder().build() : outbound;
    }

    public List<ReverseInbound> getReverseInbound()
    {
        return null == reverseInbound ? Collections.emptyList() : Collections.unmodifiableList(reverseInbound);
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
                ", reverseInbound=" + getReverseInbound() +
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
        private List<ReverseInbound> reverseInbound = new ArrayList<>();

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

        public Builder withReverseInbound( ReverseInbound reverseInbound )
        {
            this.reverseInbound.add(reverseInbound);
            return this;
        }

        public Configuration build()
        {
            return new Configuration(domain, inbound, outbound, reverseInbound );
        }
    }
}
