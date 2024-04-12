/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.config;

import java.util.Objects;

public final class EventServer
{
    private final int portNumber;
    private final boolean useEpoll;
    private final Shutdown shutdown;

    private EventServer(Builder builder)
    {
        this.portNumber = builder.portNumber;
        this.useEpoll = builder.useEpoll;
        this.shutdown = builder.shutdown;
    }

    public int getPortNumber()
    {
        return portNumber;
    }

    public boolean isUseEpoll()
    {
        return useEpoll;
    }

    public Shutdown getShutdown()
    {
        return shutdown == null ? Shutdown.newBuilder().build() : shutdown;
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
        EventServer that = (EventServer) o;
        return getPortNumber() == that.getPortNumber() && isUseEpoll() == that.isUseEpoll() && Objects.equals( getShutdown(), that.getShutdown() );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( getPortNumber(), isUseEpoll(), getShutdown() );
    }

    @Override
    public String toString()
    {
        return "EventServer{" +
                "portNumber=" + portNumber +
                ", useEpoll=" + useEpoll +
                ", shutdown=" + shutdown +
                '}';
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private int portNumber = 7698;
        private boolean useEpoll;
        private Shutdown shutdown;

        private Builder()
        {}

        public static Builder builder()
        {
            return new Builder();
        }

        public Builder withPortNumber(int portNumber)
        {
            this.portNumber = portNumber;
            return this;
        }

        public Builder withUseEpoll(boolean useEpoll)
        {
            this.useEpoll = useEpoll;
            return this;
        }

        public Builder withShutdown( Shutdown shutdown )
        {
            this.shutdown = shutdown;
            return this;
        }

        public EventServer build()
        {
            return new EventServer(this);
        }
    }
}
