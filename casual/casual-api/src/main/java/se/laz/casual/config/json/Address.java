/*
 * Copyright (c) 2022 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.config.json;

import java.util.Objects;

public final class Address
{
    private final String host;
    private final int port;

    private Address(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    public static Address of(String host, int port)
    {
        Objects.requireNonNull(host, "host can not be null");
        return new Address(host, port);
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
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
        Address address = (Address) o;
        return getPort() == address.getPort() && Objects.equals(getHost(), address.getHost());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getHost(), getPort());
    }

    @Override
    public String toString()
    {
        return "Address{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
