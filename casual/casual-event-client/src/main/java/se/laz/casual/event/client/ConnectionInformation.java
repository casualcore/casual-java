/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.client;

import java.net.InetSocketAddress;
import java.util.Objects;

public record ConnectionInformation(String host, int port)
{
    public ConnectionInformation
    {
        Objects.requireNonNull(host, "host can not be null");
    }
    public InetSocketAddress getAddress()
    {
        return new InetSocketAddress(host, port);
    }
}
