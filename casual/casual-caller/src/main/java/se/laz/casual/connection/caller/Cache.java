/*
 * Copyright (c) 2017-2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.discovery.DiscoveryReturn;
import se.laz.casual.api.queue.QueueInfo;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class Cache
{
    private final QueueCache queueCache = new QueueCache();
    private final ServiceCache serviceCache = new ServiceCache();

    public ConnectionFactoriesByPriority get(String serviceName)
    {
        return serviceCache.getOrEmpty(serviceName);
    }

    public List<ConnectionFactoryEntry> get(QueueInfo qinfo)
    {
        return Optional.ofNullable(queueCache.getAll(qinfo)).orElseGet(Collections::emptyList);
    }

    public Optional<ConnectionFactoryEntry> getSingle(QueueInfo qinfo)
    {
        return queueCache.getOrEmpty(qinfo);
    }

    public void store(String serviceName, ConnectionFactoriesByPriority entries)
    {
        Objects.requireNonNull(serviceName, "serviceInfo can not be null");
        Objects.requireNonNull(entries, "entry can not be null");

        serviceCache.store(serviceName, entries);
    }

    public void store(QueueInfo qinfo, List<ConnectionFactoryEntry> entries)
    {
        Objects.requireNonNull(qinfo, "qinfo can not be null");
        Objects.requireNonNull(entries, "entry can not be null");
        queueCache.store(qinfo, entries);
    }

    public void purgeServices()
    {
        serviceCache.clear();
    }

    public void purgeQueues()
    {
        queueCache.clear();
    }

    public void purge(ConnectionFactoryEntry connectionFactoryEntry)
    {
        serviceCache.remove(connectionFactoryEntry);
        queueCache.remove(connectionFactoryEntry);
    }
    public Map<CacheType, List<String>> getAll()
    {
        Map<CacheType, List<String>> entries = new EnumMap<>(CacheType.class);
        entries.put(CacheType.SERVICE, getServices());
        entries.put(CacheType.QUEUE, getQueues());
        return entries;
    }
    public void repopulate(DiscoveryReturn discoveryReturn, ConnectionFactoryEntry connectionFactoryEntry)
    {
        discoveryReturn.getServiceDetails().forEach(
                serviceDetails -> {
                    ConnectionFactoriesByPriority connectionFactoriesByPriority = get(serviceDetails.getName());
                    connectionFactoriesByPriority.store(Arrays.asList(serviceDetails), connectionFactoryEntry);
                    store(serviceDetails.getName(), connectionFactoriesByPriority);
                });
        discoveryReturn.getQueueDetails().forEach(
                queueDetails ->
                        store(QueueInfo.of(queueDetails.getName()), Arrays.asList(connectionFactoryEntry))
        );
    }

    public List<String> getServices()
    {
        return new ArrayList<>(serviceCache.getCachedServiceNames());
    }

    public List<String> getQueues()
    {
        return new ArrayList<>(queueCache.getCachedQueueNames());
    }

    public void removeService(String serviceName)
    {
        serviceCache.remove(serviceName);
    }
}
