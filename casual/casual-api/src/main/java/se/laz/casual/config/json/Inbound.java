/*
 * Copyright (c) 2021 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json;

import java.util.Objects;

class Inbound
{
    private final Startup startup;
    private final Boolean useEpoll;
    private final Long initialDelay;

    public Inbound( Builder builder )
    {
        this.startup = builder.startup;
        this.useEpoll = builder.useEpoll;
        this.initialDelay = builder.initialDelay;
    }

    public Startup getStartup()
    {
        return startup;
    }

    public Boolean getUseEpoll()
    {
        return useEpoll;
    }

    public Long getInitialDelay()
    {
        return initialDelay;
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
        Inbound inbound = (Inbound) o;
        return Objects.equals( startup, inbound.startup ) && Objects.equals( useEpoll, inbound.useEpoll ) && Objects.equals( initialDelay, inbound.initialDelay );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( startup, useEpoll, initialDelay );
    }

    @Override
    public String toString()
    {
        return "Inbound{" +
                "startup=" + startup +
                ", useEpoll=" + useEpoll +
                ", initialDelay=" + initialDelay +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( Inbound src )
    {
        return new Builder().withInitialDelay( src.getInitialDelay() )
                .withUseEpoll( src.getUseEpoll() )
                .withStartup( src.getStartup() );
    }

    public static final class Builder
    {
        private Startup startup;
        private Boolean useEpoll;
        private Long initialDelay;

        public Builder withStartup( Startup startup )
        {
            this.startup = startup;
            return this;
        }

        public Builder withUseEpoll( Boolean useEpoll )
        {
            this.useEpoll = useEpoll;
            return this;
        }

        public Builder withInitialDelay( Long initialDelay )
        {
            this.initialDelay = initialDelay;
            return this;
        }

        public Inbound build()
        {
            return new Inbound( this );
        }
    }
}
