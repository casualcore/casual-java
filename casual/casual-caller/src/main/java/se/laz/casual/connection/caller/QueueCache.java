/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class QueueCache
{
    private static final Logger LOG = Logger.getLogger(QueueCache.class.getName());

    private final Map<String, List<ConnectionFactoryEntry>> cacheMap = new ConcurrentHashMap<>();
    private final Map<String, ConnectionFactoryEntry> stickies = new ConcurrentHashMap<>();

    public Set<String> getCachedQueueNames()
    {
        return cacheMap.keySet();
    }

    public List<ConnectionFactoryEntry> getAll(QueueInfo queueInfo) {
        return cacheMap.get(queueInfo.getQueueName());
    }

    public Optional<ConnectionFactoryEntry> getOrEmpty(QueueInfo queueInfo)
    {
        String queueName = queueInfo.getQueueName();
        if (stickies.containsKey(queueName))
        {
            return Optional.of(stickies.get(queueName));
        }
        else if (cacheMap.containsKey(queueName))
        {
            // Prevent the unlikely case that two different threads manage to find and set different stickies
            synchronized (stickies) {
                if (stickies.containsKey(queueName))
                {
                    // While waiting another thread may have already set a sticky
                    return Optional.of(stickies.get(queueName));
                }

                List<ConnectionFactoryEntry> cachedForQueue = cacheMap.get(queueName)
                        .stream()
                        .filter(ConnectionFactoryEntry::isValid)
                        .collect(Collectors.toList());

                // We never expect more than one source for a queue. Just pick first one and stick to it
                ConnectionFactoryEntry selectedFactory = cachedForQueue.get(0);
                stickies.put(queueName, selectedFactory);

                if (cachedForQueue.size() > 1) {
                    LOG.info(() -> "Found multiple (" + cachedForQueue.size() + ") sources for queue '" + queueName
                            + "', selecting and setting sticky for CasualConnectionFactory=" + selectedFactory);
                }

                return Optional.of(selectedFactory);
            }
        }
        else
        {
            return Optional.empty();
        }
    }

    public void store(QueueInfo queueInfo, List<ConnectionFactoryEntry> entries)
    {
        cacheMap.put(queueInfo.getQueueName(), entries);
    }

    public void remove(ConnectionFactoryEntry connectionFactoryEntry)
    {
        for(Map.Entry<String, ConnectionFactoryEntry> entry : stickies.entrySet())
        {
            if(entry.getValue().getJndiName().equals(connectionFactoryEntry.getJndiName()))
            {
                stickies.remove(entry.getKey());
            }
        }
        for(Map.Entry<String, List<ConnectionFactoryEntry>> entry : cacheMap.entrySet())
        {
            List<ConnectionFactoryEntry> l = entry.getValue();
            l.removeIf(cachedEntry -> Objects.equals(cachedEntry.getJndiName(), connectionFactoryEntry.getJndiName()));
            if(l.isEmpty())
            {
                cacheMap.remove(entry.getKey());
            }
        }
    }

    public void clear()
    {
        cacheMap.clear();
        stickies.clear();
    }
}
