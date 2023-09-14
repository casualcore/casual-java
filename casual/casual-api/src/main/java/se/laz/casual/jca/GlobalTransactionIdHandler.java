/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class GlobalTransactionIdHandler
{
    private final Set<GlobalTransactionId> preparedGtrids;
    private final Map<DomainId, Set<GlobalTransactionId>> domainIdToPreparedGtrids;

    public GlobalTransactionIdHandler(Set<GlobalTransactionId> preparedGtrids, Map<DomainId, Set<GlobalTransactionId>> domainIdToPreparedGtrids)
    {
        this.preparedGtrids = preparedGtrids;
        this.domainIdToPreparedGtrids = domainIdToPreparedGtrids;
    }

    public static GlobalTransactionIdHandler of()
    {
        return of(ConcurrentHashMap.newKeySet(), new ConcurrentHashMap<>());
    }

    public static GlobalTransactionIdHandler of(Set<GlobalTransactionId> preparedGtrids, Map<DomainId, Set<GlobalTransactionId>> domainIdToPreparedGtrids)
    {
        Objects.requireNonNull(preparedGtrids, "preparedGtrids can not be null");
        Objects.requireNonNull(domainIdToPreparedGtrids, "domainIdToGtrids can not be null");
        return new GlobalTransactionIdHandler(preparedGtrids, domainIdToPreparedGtrids);
    }

    public void addGtrid(GlobalTransactionId globalTransactionId, DomainId belongsTo)
    {
        Objects.requireNonNull(globalTransactionId);
        preparedGtrids.add(globalTransactionId);
        domainIdToPreparedGtrids.computeIfAbsent(belongsTo, domainId -> ConcurrentHashMap.newKeySet()).add(globalTransactionId);
    }

    public boolean exists(GlobalTransactionId globalTransactionId)
    {
        Objects.requireNonNull(globalTransactionId);
        return preparedGtrids.contains(globalTransactionId);
    }

    public void removeGtrid(GlobalTransactionId globalTransactionId)
    {
        Objects.requireNonNull(globalTransactionId);
        preparedGtrids.remove(globalTransactionId);
    }

    public void removeAllGtridsFor(DomainId domainId)
    {
        Set<GlobalTransactionId> domainIds = domainIdToPreparedGtrids.remove(domainId);
        if(null != domainIds)
        {
            preparedGtrids.removeAll(domainIds);
        }
    }

    public Set<GlobalTransactionId> getPreparedGtrids()
    {
        return Collections.unmodifiableSet(preparedGtrids);
    }

    public Map<DomainId, Set<GlobalTransactionId>> getDomainIdToPreparedGtrids()
    {
        return Collections.unmodifiableMap(domainIdToPreparedGtrids);
    }

}
