/*
 * Copyright (c) 2022 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.config;

import java.util.Objects;

public final class ReverseInbound
{
    private final String host;
    private final Integer port;
    private final Integer size;
    private final Long maxConnectionBackoffMillis;

    public ReverseInbound( Builder builder )
    {
        this.host = builder.host;
        this.port = builder.port;
        this.size = builder.size;
        this.maxConnectionBackoffMillis = builder.maxConnectionBackoffMillis;
    }

    public String getHost()
    {
        return host;
    }

    public Integer getPort()
    {
        return port;
    }

    public Integer getSize()
    {
        return size;
    }

    public Long getMaxConnectionBackoffMillis()
    {
        return maxConnectionBackoffMillis;
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
        ReverseInbound that = (ReverseInbound) o;
        return Objects.equals( host, that.host ) && Objects.equals( port, that.port ) && Objects.equals( size, that.size ) && Objects.equals( maxConnectionBackoffMillis, that.maxConnectionBackoffMillis );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( host, port, size, maxConnectionBackoffMillis );
    }

    @Override
    public String toString()
    {
        return "ReverseInbound{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", size=" + size +
                ", maxConnectionBackoffMillis=" + maxConnectionBackoffMillis +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( ReverseInbound src )
    {
        return new Builder().withHost( src.getHost() )
                .withPort( src.getPort() )
                .withSize( src.getSize() )
                .withMaxConnectionBackoffMillis( src.getMaxConnectionBackoffMillis() );
    }

    public static final class Builder
    {
        private String host;
        private Integer port;
        private Integer size;
        private Long maxConnectionBackoffMillis;

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

        public Builder withSize( Integer size )
        {
            this.size = size;
            return this;
        }

        public Builder withMaxConnectionBackoffMillis( Long maxConnectionBackoffMillis )
        {
            this.maxConnectionBackoffMillis = maxConnectionBackoffMillis;
            return this;
        }

        public ReverseInbound build()
        {
            return new ReverseInbound( this );
        }
    }
}
