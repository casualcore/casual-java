/*
 * Copyright (c) 2017-2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class Cache
{
    private final Map<QueueInfo, List<ConnectionFactoryEntry>> queueCache = new ConcurrentHashMap<>();
    private final ServiceCache serviceCache = new ServiceCache();

    public ConnectionFactoriesByPriority get(String serviceName)
    {
        return serviceCache.getOrEmpty(serviceName);
    }

    public List<ConnectionFactoryEntry> get(QueueInfo qinfo)
    {
        return queueCache.getOrDefault(qinfo, Collections.emptyList());
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
        queueCache.put(qinfo, entries);
    }

    public void purgeServices()
    {
        serviceCache.clear();
    }

    public void purgeQueues()
    {
        queueCache.clear();
    }

    public List<String> getServices()
    {
        return new ArrayList<>(serviceCache.getCachedServiceNames());
    }

    public List<String> getQueues()
    {
        return queueCache.keySet().stream().map(QueueInfo::toString).collect(Collectors.toList());
    }
}
