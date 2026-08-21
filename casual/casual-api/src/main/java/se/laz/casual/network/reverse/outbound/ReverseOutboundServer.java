/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.reverse.outbound;

public interface ReverseOutboundServer
{
    String getName();
    int getPort();
    void deactivate();
}
