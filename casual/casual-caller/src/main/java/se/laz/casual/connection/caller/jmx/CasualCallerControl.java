/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller.jmx;

import se.laz.casual.connection.caller.Cache;
import se.laz.casual.connection.caller.ConnectionFactoryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CasualCallerControl implements CasualCallerControlMBean
{
    private final Cache cache;

    public CasualCallerControl(Cache cache)
    {
        this.cache = cache;
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
    public List<String> factoriesChecked(String serviceName)
    {
        List<String> factories = new ArrayList<>(cache.get(serviceName).getCheckedFactoriesForService());
        factories.sort(String::compareTo);
        return factories;
    }

    @Override
    public List<String> factoriesContaining(String serviceName)
    {
        return cache.get(serviceName).randomizeWithPriority().stream().map(ConnectionFactoryEntry::getJndiName).sorted().collect(Collectors.toList());
    }
}
