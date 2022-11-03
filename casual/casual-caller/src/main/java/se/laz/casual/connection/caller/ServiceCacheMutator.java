/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

@FunctionalInterface
public interface ServiceCacheMutator
{
    void removeFromServiceCache(String serviceName);
}
