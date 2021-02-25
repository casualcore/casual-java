/*
 * Copyright (c) 2017-2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class Cache
{
    private final Map<QueueInfo, List<ConnectionFactoryEntry>> queueCache = new ConcurrentHashMap<>();
    private final Map<String, List<ConnectionFactoryEntry>> serviceCache = new ConcurrentHashMap<>();

    public List<ConnectionFactoryEntry> get(String serviceName)
    {
        return serviceCache.getOrDefault(serviceName, Collections.emptyList());
    }

    public List<ConnectionFactoryEntry> get(QueueInfo qinfo)
    {
         return queueCache.getOrDefault(qinfo, Collections.emptyList());
    }

    public void store(String serviceName, List<ConnectionFactoryEntry> entries)
    {
        Objects.requireNonNull(serviceName, "serviceInfo can not be null");
        Objects.requireNonNull(entries, "entry can not be null");
        serviceCache.put(serviceName, entries);
    }

    public void store(QueueInfo qinfo, List<ConnectionFactoryEntry> entries)
    {
        Objects.requireNonNull(qinfo, "qinfo can not be null");
        Objects.requireNonNull(entries, "entry can not be null");
        queueCache.put(qinfo, entries);
    }

}
