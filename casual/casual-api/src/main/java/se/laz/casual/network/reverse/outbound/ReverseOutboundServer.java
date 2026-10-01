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

    /**
     * Stops accepting connections and closes all established connections managed by this server.
     *
     * <p>The call that performs deactivation waits uninterruptibly until the listener channel
     * and all tracked connection channels close. Repeated calls have no effect. This method does
     * not shut down the shared network event loops.
     */
    void deactivate();
}
