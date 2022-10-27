/*
 * Copyright (c) 2021-, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

@FunctionalInterface
public interface NetworkListener
{
    void disconnected(Exception reason);
}
