/*
 * Copyright (c) 2017 - 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        if (!cachedEntries.isEmpty())
        {
            return cachedEntries;
        }
        List<ConnectionFactoryEntry> newEntries = lookup.find(qinfo, connectionFactoryProvider.get());
        if (!newEntries.isEmpty())
        {
            cache.store(qinfo, newEntries);
        }
        return newEntries;
    }

    @Override
    public List<ConnectionFactoryEntry> get(String serviceName)
    {
        // Services by cache
        Objects.requireNonNull(serviceName, "serviceName can not be null");
        ConnectionFactoriesByPriority cachedEntries = cache.get(serviceName);
        if (!cachedEntries.isEmpty() && cachedEntries.hasCheckedAllValid(connectionFactoryProvider.get()))
        {
            // Using cached entries and no further discovery is appropriate
            return cachedEntries.randomizeWithPriority();
        }

        // Services by lookup. Only lookup against previously unresolved connection factories.
        ConnectionFactoriesByPriority newEntries = lookup.find(serviceName, connectionFactoryProvider.get()
                .stream()
                .filter(entry -> !cache.get(serviceName).isResolved(entry.getJndiName()))
                .collect(Collectors.toList()));
        if (!newEntries.isEmpty())
        {
            cache.store(serviceName, newEntries);
            return cache.get(serviceName).randomizeWithPriority();
        }

        // If we only have a bunch of invalid connection-factories to report it should be done so,
        // because a different error may be reported depending on if the service has no known backend
        // or if none of the known backends are available
        return cachedEntries.isEmpty() ? Collections.emptyList() : cachedEntries.randomizeWithPriority();
    }
}
