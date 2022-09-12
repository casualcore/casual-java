/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller.jmx;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.Cache;
import se.laz.casual.connection.caller.ConnectionFactoryEntryStore;
import se.laz.casual.connection.caller.pool.PoolManager;
import se.laz.casual.connection.caller.entities.CacheEntry;
import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry;
import se.laz.casual.connection.caller.entities.Pool;

import java.util.List;
import java.util.stream.Collectors;

public class CasualCallerControl implements CasualCallerControlMBean
{
    private final ConnectionFactoryEntryStore connectionFactoryEntryStore;
    private final Cache cache;
    private final PoolManager poolManager;

    public CasualCallerControl(ConnectionFactoryEntryStore connectionFactoryEntryStore, Cache cache, PoolManager poolManager)
    {
        this.connectionFactoryEntryStore = connectionFactoryEntryStore;
        this.cache = cache;
        this.poolManager = poolManager;
    }

    public static CasualCallerControl of(ConnectionFactoryEntryStore connectionFactoryEntryStore, Cache cache, PoolManager poolManager)
    {
        return new CasualCallerControl(connectionFactoryEntryStore, cache, poolManager);
    }

    @Override
    public List<String> connectionFactoryJNDINames()
    {
        return connectionFactoryEntryStore.get()
                                          .stream()
                                          .map(ConnectionFactoryEntry::toString)
                                          .collect(Collectors.toList());
    }

    @Override
    public List<String> currentPools()
    {
        return poolManager.getPools()
                          .stream()
                          .map(Pool::toString)
                          .collect(Collectors.toList());
    }

    @Override
    public void purgeServiceCache()
    {
        cache.purgeServiceCache();
    }

    @Override
    public List<String> cachedServices()
    {
        return cache.getCachedServices();
    }

    @Override
    public List<String> allServices()
    {
        return cache.getAllServices();
    }

    @Override
    public List<String> poolsForService(String serviceName)
    {
        return cache.get(ServiceInfo.of(serviceName))
                    .stream()
                    .map(CacheEntry::toString)
                    .collect(Collectors.toList());
    }

    @Override
    public void purgeQueueCache()
    {
        cache.purgeQueueCache();
    }

    @Override
    public List<String> cachedQueues()
    {
        return cache.getCachedSQueues();
    }

    @Override
    public List<String> allQueues()
    {
        return cache.getAllQueues();
    }

    @Override
    public String poolForQueue(String queueName)
    {
        CacheEntry cacheEntry = cache.get(QueueInfo.of(queueName)).orElse(null);
        return null != cacheEntry ? cacheEntry.toString() : null;
    }

}
