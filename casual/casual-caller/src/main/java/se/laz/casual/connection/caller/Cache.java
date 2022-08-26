/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.entities.CacheEntry;
import se.laz.casual.connection.caller.entities.MatchingEntry;

import java.util.List;
import java.util.Optional;

public interface Cache
{
    void store(List<MatchingEntry> matchingEntries);
    List<CacheEntry> get(ServiceInfo serviceInfo);
    Optional<CacheEntry> get(QueueInfo queueInfo);
    List<String> getCachedServices();
    List<String> getCachedSQueues();
    List<String> getAllServices();
    List<String> getAllQueues();
    void purgeServiceCache();
    void purgeQueueCache();
}
