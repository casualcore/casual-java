/*
 * Copyright (c) 2022 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.config.json;

import java.util.Objects;

final class ReverseInbound
{
    private final Address address;
    private final Integer size;
    private final Long maxConnectionBackoffMillis;

    private ReverseInbound( Builder builder)
    {
        this.address = builder.address;
        this.size = builder.size;
        this.maxConnectionBackoffMillis = builder.maxConnectionBackoffMillis;
    }

    public Address getAddress()
    {
        return address;
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
        return Objects.equals( address, that.address ) && Objects.equals( size, that.size ) && Objects.equals( maxConnectionBackoffMillis, that.maxConnectionBackoffMillis );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( address, size, maxConnectionBackoffMillis );
    }

    @Override
    public String toString()
    {
        return "ReverseInbound{" +
                "address=" + address +
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
        return new Builder().withAddress( src.getAddress() )
                .withSize( src.getSize() )
                .withMaxConnectionBackoffMillis( src.getMaxConnectionBackoffMillis() );
    }

    public static final class Builder
    {
        private Address address;
        private Integer size;
        private Long maxConnectionBackoffMillis;

        public Builder withAddress( Address address )
        {
            this.address = address;
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
