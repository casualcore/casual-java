/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller.jmx;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.connection.caller.Cache;
import se.laz.casual.connection.caller.ConnectionFactoryEntry;
import se.laz.casual.connection.caller.ConnectionFactoryEntryStore;
import se.laz.casual.connection.caller.config.ConfigurationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CasualCallerControl implements CasualCallerControlMBean
{
    private final Cache cache;
    private final ConnectionFactoryEntryStore connectionFactoryEntryStore;

    public CasualCallerControl(Cache cache, ConnectionFactoryEntryStore connectionFactoryEntryStore)
    {
        this.cache = cache;
        this.connectionFactoryEntryStore = connectionFactoryEntryStore;
    }

    @Override
    public List<String> validPools()
    {
        return connectionFactoryEntryStore.get()
                .stream()
                .filter(ConnectionFactoryEntry::isValid)
                .map(ConnectionFactoryEntry::getJndiName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> invalidPools()
    {
        return connectionFactoryEntryStore.get()
                .stream()
                .filter(ConnectionFactoryEntry::isInvalid)
                .map(ConnectionFactoryEntry::getJndiName)
                .collect(Collectors.toList());
    }


    @Override
    public void purgeServiceCache()
    {
        cache.purgeServices();
    }

    @Override
    public void purgeQueueCache()
    {
        cache.purgeQueues();
    }

    @Override
    public List<String> cachedServices()
    {
        return cache.getServices();
    }

    @Override
    public List<String> cachedQueues()
    {
        return cache.getQueues();
    }

    @Override
    public List<String> poolsCheckedForService(String serviceName)
    {
        List<String> factories = new ArrayList<>(cache.get(serviceName).getCheckedFactoriesForService());
        factories.sort(String::compareTo);
        return factories;
    }

    @Override
    public List<String> poolsContainingService(String serviceName)
    {
        return cache
                .get(serviceName)
                .randomizeWithPriority()
                .stream()
                .map(ConnectionFactoryEntry::getJndiName)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> queueInPools(String queueName)
    {
        return cache
                .get(QueueInfo.of(queueName))
                .stream()
                .map(ConnectionFactoryEntry::getJndiName)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public String getQueueStickiedPool(String queueName)
    {
        Optional<ConnectionFactoryEntry> queueEntry = cache.getSingle(QueueInfo.of(queueName));
        return queueEntry.map(ConnectionFactoryEntry::getJndiName).orElse(null);
    }

    @Override
    public boolean transactionStickyEnabled()
    {
        return ConfigurationService.getInstance().getConfiguration().isTransactionStickyEnabled();
    }
}
