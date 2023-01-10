/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.Objects;
import java.util.Optional;

public class Inbound
{
    public static final String CASUAL_INBOUND_STARTUP_MODE = "CASUAL_INBOUND_STARTUP_MODE";
    public static final String CASUAL_INBOUND_USE_EPOLL = "CASUAL_INBOUND_USE_EPOLL";
    public static final String CASUAL_INBOUND_INITIAL_DELAY_ENV_NAME = "CASUAL_INBOUND_INITIAL_DELAY";

    private final Startup startup;
    private final boolean useEpoll;
    private final long initialDelay;

    public Inbound( Builder builder )
    {
        this.startup = builder.startup;
        this.useEpoll = builder.useEpoll;
        this.initialDelay = builder.initialDelay;
    }

    public Startup getStartup()
    {
        return startup == null ? Startup.newBuilder().build() : startup;
    }

    public boolean isUseEpoll()
    {
        return useEpoll;
    }

    public long getInitialDelay()
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
        return isUseEpoll() == inbound.isUseEpoll() && Objects.equals( getStartup(), inbound.getStartup() );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( getStartup(), isUseEpoll() );
    }

    @Override
    public String toString()
    {
        return "Inbound{" +
                "startup=" + startup +
                ", useEpoll=" + useEpoll +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private Startup startup;
        private Boolean useEpoll;
        private Long initialDelay;

        private Builder()
        {
        }

        public Builder withUseEpoll( boolean useEpoll )
        {
            this.useEpoll = useEpoll;
            return this;
        }

        public Builder withStartup( Startup startup )
        {
            this.startup = startup;
            return this;
        }

        public Builder withInitialDelay(long initialDelay)
        {
            this.initialDelay = initialDelay;
            return this;
        }

        public Inbound build()
        {
            if(startup == null)
            {
                // try to get startup mode from env variable, otherwise default
                String startupMode = Optional.ofNullable(System.getenv(CASUAL_INBOUND_STARTUP_MODE)).orElse(null);
                if(null != startupMode)
                {
                    Mode mode = Mode.fromName(startupMode);
                    startup = Startup.newBuilder().withMode(mode).build();
                }
            }
            if( useEpoll == null )
            {
                useEpoll = Boolean.parseBoolean( Optional.ofNullable( System.getenv( CASUAL_INBOUND_USE_EPOLL ) ).orElse( "false" ) );
            }
            if( initialDelay == null )
            {
                initialDelay = Optional.ofNullable( System.getenv( CASUAL_INBOUND_INITIAL_DELAY_ENV_NAME ) )
                                       .map( delay -> delay.isEmpty() ? 0L : Long.parseLong( delay ) )
                                       .orElse(0L );
            }
            return new Inbound( this );
        }
    }
}
