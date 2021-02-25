/*
 * Copyright (c) 2017 - 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

public class ConnectionFactoryLookupService implements ConnectionFactoryLookup
{
    @Inject
    private ConnectionFactoryProvider connectionFactoryProvider;
    @Inject
    private Cache cache;
    @Inject
    private Lookup lookup;

    @Override
    public List<ConnectionFactoryEntry> get(QueueInfo qinfo)
    {
        Objects.requireNonNull(qinfo, "qinfo can not be null");
        List<ConnectionFactoryEntry> cachedEntries = cache.get(qinfo);
        if(!cachedEntries.isEmpty())
        {
            return cachedEntries;
        }
        List<ConnectionFactoryEntry> newEntries = lookup.find(qinfo, connectionFactoryProvider.get());
        if(!newEntries.isEmpty())
        {
            cache.store(qinfo, newEntries);
        }
        return newEntries;
    }

    @Override
    public List<ConnectionFactoryEntry> get(String serviceName)
    {
        Objects.requireNonNull(serviceName, "serviceName can not be null");
        List<ConnectionFactoryEntry> cachedEntries = cache.get(serviceName);
        if(!cachedEntries.isEmpty())
        {
            return cachedEntries;
        }
        List<ConnectionFactoryEntry> newEntries = lookup.find(serviceName, connectionFactoryProvider.get());
        if(!newEntries.isEmpty())
        {
            cache.store(serviceName, newEntries);
        }
        return newEntries;
    }

}
