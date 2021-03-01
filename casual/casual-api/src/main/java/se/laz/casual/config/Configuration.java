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

    public Configuration( Inbound inbound )
    {
        this.inbound = inbound;
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
        return Objects.equals( getInbound(), that.getInbound() );
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

        private Builder()
        {
        }

        public Builder withInbound( Inbound inbound )
        {
            this.inbound = inbound;
            return this;
        }

        public Configuration build()
        {
            return new Configuration( inbound );
        }
    }
}
