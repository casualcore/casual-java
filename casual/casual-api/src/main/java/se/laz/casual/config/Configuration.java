/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.Objects;

public class Configuration
{
    private final Inbound inbound;
    private final Domain domain;

    public Configuration(Domain domain, Inbound inbound )
    {
        this.domain = domain;
        this.inbound = inbound;
    }

    public Domain getDomain()
    {
        return domain == null ? Domain.of(null) : domain;
    }

    public Inbound getInbound()
    {
        return inbound == null ? Inbound.newBuilder().build() : inbound;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        Configuration that = (Configuration) o;
        return Objects.equals( getDomain(), that.getDomain() ) && Objects.equals( getInbound(), that.getInbound() );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( getInbound() );
    }

    @Override
    public String toString()
    {
        return "Configuration{" +
                "domain=" + getDomain() + ", " +
                "inbound=" + getInbound() +
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

        public Configuration build()
        {
            return new Configuration(domain, inbound );
        }
    }
}
