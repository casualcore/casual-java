/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json;

import java.util.Objects;
import java.util.Optional;

public class Shutdown
{
    public static final String CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS_ENV_NAME = "CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS";
    public static final String CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS_ENV_NAME = "CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS";
    public static final long DEFAULT_QUIET_PERIOD_MILLIS = 2000;
    public static final long DEFAULT_TIMEOUT_MILLIS = 15000;

    private final long quietPeriod;
    private final long timeout;

    public Shutdown( Builder builder )
    {
        this.quietPeriod = builder.quietPeriod;
        this.timeout = builder.timeout;
    }

    public long getQuietPeriod()
    {
        return quietPeriod;
    }

    public long getTimeout()
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
        return quietPeriod == shutdown.quietPeriod && timeout == shutdown.timeout;
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


    public static final class Builder
    {
        private Long quietPeriod;
        private Long timeout;

        private Builder()
        {
        }

        public Builder withQuietPeriod( long quietPeriod )
        {
            this.quietPeriod = quietPeriod;
            return this;
        }

        public Builder withTimeout( long timeout )
        {
            this.timeout = timeout;
            return this;
        }

        public Shutdown build()
        {
            if( quietPeriod == null )
            {
                quietPeriod = Long.parseLong(
                        Optional.ofNullable(System.getenv(CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS_ENV_NAME))
                                .orElse( String.valueOf( DEFAULT_QUIET_PERIOD_MILLIS ) ) );
            }

            if( timeout == null )
            {
                timeout = Long.parseLong(
                        Optional.ofNullable(System.getenv(CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS_ENV_NAME))
                                .orElse( String.valueOf( DEFAULT_TIMEOUT_MILLIS ) ) );
            }

            return new Shutdown( this );
        }
    }
}
