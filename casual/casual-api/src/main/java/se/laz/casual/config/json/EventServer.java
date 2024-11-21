/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.config.json;

import java.util.Objects;

final class EventServer
{
    private final Integer portNumber;
    private final Boolean useEpoll;
    private final Shutdown shutdown;

    private EventServer( Builder builder )
    {
        this.portNumber = builder.portNumber;
        this.useEpoll = builder.useEpoll;
        this.shutdown = builder.shutdown;
    }

    public Integer getPortNumber()
    {
        return portNumber;
    }

    public Boolean getUseEpoll()
    {
        return useEpoll;
    }

    public Shutdown getShutdown()
    {
        return shutdown;
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
        return Objects.equals( portNumber, that.portNumber ) && Objects.equals( useEpoll, that.useEpoll ) && Objects.equals( shutdown, that.shutdown );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( portNumber, useEpoll, shutdown );
    }

    @Override
    public String toString()
    {
        return "EventServer{" +
                "port=" + portNumber +
                ", useEpoll=" + useEpoll +
                ", shutdown=" + shutdown +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( EventServer src )
    {
        return new Builder().withPort( src.getPortNumber() )
                .withUseEpoll( src.getUseEpoll() )
                .withShutdown( src.getShutdown() );
    }

    public static final class Builder
    {
        private Integer portNumber;
        private Boolean useEpoll;
        private Shutdown shutdown;

        public Builder withPort( Integer port )
        {
            this.portNumber = port;
            return this;
        }

        public Builder withUseEpoll( Boolean useEpoll )
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
            return new EventServer( this );
        }
    }
}
