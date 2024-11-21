/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json;

import java.util.Objects;

class Shutdown
{
    private final Long quietPeriod;
    private final Long timeout;

    private Shutdown( Builder builder )
    {
        this.quietPeriod = builder.quietPeriod;
        this.timeout = builder.timeout;
    }

    public Long getQuietPeriod()
    {
        return quietPeriod;
    }

    public Long getTimeout()
    {
        return timeout;
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
        Shutdown shutdown = (Shutdown) o;
        return Objects.equals( quietPeriod, shutdown.quietPeriod ) && Objects.equals( timeout, shutdown.timeout );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( quietPeriod, timeout );
    }

    @Override
    public String toString()
    {
        return "Shutdown{" +
                "quietPeriod=" + quietPeriod +
                ", timeout=" + timeout +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( Shutdown src )
    {
        return new Builder().withQuietPeriod( src.getQuietPeriod() ).withTimeout( src.getTimeout() );
    }

    public static final class Builder
    {
        private Long quietPeriod;
        private Long timeout;

        public Builder withQuietPeriod( Long quietPeriod )
        {
            this.quietPeriod = quietPeriod;
            return this;
        }

        public Builder withTimeout( Long timeout )
        {
            this.timeout = timeout;
            return this;
        }

        public Shutdown build()
        {
            return new Shutdown( this );
        }
    }
}
