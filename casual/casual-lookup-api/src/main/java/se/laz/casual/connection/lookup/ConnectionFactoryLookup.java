/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.lookup;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Lookup a suitable JNDI name for a CasualConnectionFactory that can serve a QueueInfo/ServiceInfo request
 *
 * The lookup is based on jndi names for CasualConnectionFactories that are read from a configuration file
 *
 * You set the configuration file via -Dcasual.connection.lookup.config=/path/to/config-file.json
 * Do note, the path has to be absolute
 *
 * The format of the json file is:
 * {
 *     "jndinames" : ["configured-jndi-name-one", "configured-jndi-name-two"]
 * }
 *
 * Note, results are cached - thus a successful lookup is only ever done once
 * You can clear the cache by using {@link #evict(QueueInfo) evict} or {@link #evict(ServiceInfo) evict}
 */
public interface ConnectionFactoryLookup
{
    /**
     * Uses an initial context with the supplied environmental properties
     * Returns a jndi name to a CasualConnectionFactory if it has a queue matching qinfo
     * otherwise empty
     *
     * @param qinfo
     * @param initialContextEnvironment
     * @return an Optional jndi name depending on if the queue is available via any known CasualConnectionFactory
     */
    Optional<String> getJNDIName(QueueInfo qinfo, Map<?,?> initialContextEnvironment);

    /**
     * Same as {@link #getJNDIName(QueueInfo, Map) getJNDIName} but it uses no properties for initial context
     * @param qinfo
     * @return an Optional jndi name depending on if the queue is available via any known CasualConnectionFactory
     */
    Optional<String> getJNDIName(QueueInfo qinfo);

    /**
     * Uses an initial context with the supplied environmental properties
     * Returns a jndi name to a CasualConnectionFactory if it has a service matching serviceInfo
     * otherwise empty
     *
     * @param serviceInfo
     * @param initialContextEnvironment
     * @return an Optional jndi name depending on if the service is available via any known CasualConnectionFactory
     */
    Optional<String> getJNDIName(ServiceInfo serviceInfo, Map<?,?> initialContextEnvironment);

    /**
     * Same as {@link #getJNDIName(ServiceInfo, Map) getJNDIName} but it uses no properties for initial context
     * @param serviceInfo
     * @return an Optional jndi name depending on if the service is available via any known CasualConnectionFactory
     */
    Optional<String> getJNDIName(ServiceInfo serviceInfo);

    /**
     * Removes entry from cache, does nothing if it does not exist
     * @param qinfo
     */
    void evict(QueueInfo qinfo);

    /**
     * Removes entry from cache, does nothing if it does not exist
     * @param serviceInfo
     */
    void evict(ServiceInfo serviceInfo);
}
