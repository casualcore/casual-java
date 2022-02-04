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
    private final Startup startup;

    public Inbound( Startup startup )
    {
        this.startup = startup;
    }

    public Startup getStartup()
    {
        return startup == null ? Startup.newBuilder().build() : startup;
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
        return Objects.equals( getStartup(), inbound.getStartup() );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( getStartup() );
    }

    @Override
    public String toString()
    {
        return "Inbound{" +
                "startup=" + getStartup() +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private Startup startup;

        private Builder()
        {
        }

        public Builder withStartup( Startup startup )
        {
            this.startup = startup;
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
                    return new Inbound(Startup.newBuilder().withMode(mode).build());
                }
            }
            return new Inbound( startup );
        }
    }
}
