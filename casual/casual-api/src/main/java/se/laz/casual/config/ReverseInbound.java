/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.config;

import java.util.Objects;

public final class ReverseInbound
{
    private static final int DEFAULT_SIZE = 1;
    private static final long DEFAULT_MAX_CONNECTION_BACKOFF_MILLIS = 30000;
    private final Address address;
    private Integer size;
    private Long maxConnectionBackoffMillis;

    private ReverseInbound(Address address, int size, long maxConnectionBackoffMillis)
    {
        this.address = address;
        this.size = size;
        this.maxConnectionBackoffMillis = maxConnectionBackoffMillis;
    }

    public static ReverseInbound of(Address address, int size, long maxBackoffMillis)
    {
        Objects.requireNonNull(address, "address can not be null");
        return new ReverseInbound(address, size, maxBackoffMillis);
    }

    public static ReverseInbound of(Address address, int size)
    {
        Objects.requireNonNull(address, "address can not be null");
        return new ReverseInbound(address, size, DEFAULT_MAX_CONNECTION_BACKOFF_MILLIS);
    }

    public static ReverseInbound of(Address address)
    {
        return of(address, DEFAULT_SIZE);
    }

    public Address getAddress()
    {
        return address;
    }

    public int getSize()
    {
        return null == size ? DEFAULT_SIZE : size;
    }

    public long getMaxConnectionBackoffMillis()
    {
        return null == maxConnectionBackoffMillis ? DEFAULT_MAX_CONNECTION_BACKOFF_MILLIS : maxConnectionBackoffMillis;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ReverseInbound that = (ReverseInbound) o;
        return getMaxConnectionBackoffMillis() == that.getMaxConnectionBackoffMillis() && getSize() == that.getSize() && Objects.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getAddress(), getSize(), getMaxConnectionBackoffMillis());
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
}
