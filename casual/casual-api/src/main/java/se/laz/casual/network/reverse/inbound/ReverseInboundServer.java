/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.reverse.inbound;

import java.net.InetSocketAddress;

public interface ReverseInboundServer
{
    InetSocketAddress getAddress();
    void prepareShutdown();
}
