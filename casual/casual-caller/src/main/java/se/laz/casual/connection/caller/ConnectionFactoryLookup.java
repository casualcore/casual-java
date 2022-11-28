/*
 * Copyright (c) 2017-2021. The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;

import java.util.List;
import java.util.Optional;

/**
 * Lookup CacheEntries that can serve the QueueInfo/ServiceInfo request
 *
 * The lookup is based on finding, in runtime, instances of CasualConnectionFactory.
 *
 * On WLS the jndi root is expected to be eis
 * On JBOSS it is expected to be java:/eis
 *
 * The connection factories have to be configured in your application server under those roots depending on your application server
 *
 * Note, results are cached - thus a successful lookup is only ever done once
 *
 */
public interface ConnectionFactoryLookup extends ServiceCacheMutator
{
    /**
     * Lookup cache entry for a queue.
     *
     * @param qinfo queue to lookup.
     * @return an optional CacheEntry that may be empty if no connection factory handles the requested queue
     */
    Optional<ConnectionFactoryEntry> get(QueueInfo qinfo);


    /**
     * Lookup cache entry for a service.
     *
     * @param serviceName - the name of the service
     * @return a List of 0-n CacheEntries
     */
    List<ConnectionFactoryEntry> get(String serviceName);
}
