/*
 * Copyright (c) 2022 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.config.json;

import java.util.Objects;

final class Address
{
    private final String host;
    private final Integer port;

    public Address( Builder builder )
    {
        this.host = builder.host;
        this.port = builder.port;
    }

    public String getHost()
    {
        return host;
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
        Address address = (Address) o;
        return Objects.equals( host, address.host ) && Objects.equals( port, address.port );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( host, port );
    }

    @Override
    public String toString()
    {
        return "Address{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }


    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( Address src )
    {
        return new Builder().withHost( src.host ).withPort( src.port );
    }

    public static final class Builder
    {
        private String host;
        private Integer port;

        public Builder withHost( String host )
        {
            this.host = host;
            return this;
        }

        public Builder withPort( Integer port )
        {
            this.port = port;
            return this;
        }

        public Address build()
        {
            return new Address( this );
        }
    }
}
