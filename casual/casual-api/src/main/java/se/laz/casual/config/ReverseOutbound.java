/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.config;

import java.util.Objects;

/**
 * Reverse Outbound configuration.
 * A reverse outbound connection is initiated by the EIS connecting to us.
 * After connect we switch roles and act as an outbound client.
 * Identified by unique name + port.
 */
public final class ReverseOutbound
{
    private final String name;
    private final Integer port;

    private ReverseOutbound( Builder builder )
    {
        this.name = builder.name;
        this.port = builder.port;
    }

    public String getName()
    {
        return name;
    }

    public Integer getPort()
    {
        return port;
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
        ReverseOutbound that = (ReverseOutbound) o;
        return Objects.equals( name, that.name ) && Objects.equals( port, that.port );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( name, port );
    }

    @Override
    public String toString()
    {
        return "ReverseOutbound{" +
                "name='" + name + '\'' +
                ", port=" + port +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( ReverseOutbound src )
    {
        return new Builder().withName( src.getName() )
                .withPort( src.getPort() );
    }

    public static final class Builder
    {
        private String name;
        private Integer port;

        public Builder withName( String name )
        {
            this.name = name;
            return this;
        }

        public Builder withPort( Integer port )
        {
            this.port = port;
            return this;
        }

        public ReverseOutbound build()
        {
            Objects.requireNonNull( name, "reverse outbound configuration: name can not be null" );
            Objects.requireNonNull( port, "reverse outbound configuration: port can not be null" );
            return new ReverseOutbound( this );
        }
    }
}
