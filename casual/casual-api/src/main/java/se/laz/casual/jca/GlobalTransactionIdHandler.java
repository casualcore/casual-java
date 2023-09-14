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
import java.util.stream.Collectors;

public final class GlobalTransactionIdHandler
{
    private static final String GLOBAL_TRANSACTION_ID_CAN_NOT_BE_NULL = "globalTransactionId can not be null";
    private static final String DOMAIN_ID_CAN_NOT_BE_NULL = "domainId can not be null";
    private final Map<DomainId, Set<GlobalTransactionId>> domainIdToPreparedGtrids = new ConcurrentHashMap<>();

    private GlobalTransactionIdHandler()
    {}

    public static GlobalTransactionIdHandler of()
    {
        return new GlobalTransactionIdHandler();
    }

    public void addGtrid(GlobalTransactionId globalTransactionId, DomainId belongsTo)
    {
        Objects.requireNonNull(globalTransactionId, GLOBAL_TRANSACTION_ID_CAN_NOT_BE_NULL);
        Objects.requireNonNull(belongsTo, "belongsTo can not be null");
        domainIdToPreparedGtrids.computeIfAbsent(belongsTo, domainId -> ConcurrentHashMap.newKeySet()).add(globalTransactionId);
    }

    public boolean exists(GlobalTransactionId globalTransactionId)
    {
        Objects.requireNonNull(globalTransactionId, GLOBAL_TRANSACTION_ID_CAN_NOT_BE_NULL);
        return domainIdToPreparedGtrids.values().stream()
                                       .anyMatch(set -> set.contains(globalTransactionId));
    }

    public void removeGtrid(GlobalTransactionId globalTransactionId, DomainId domainId)
    {
        Objects.requireNonNull(globalTransactionId, GLOBAL_TRANSACTION_ID_CAN_NOT_BE_NULL);
        Objects.requireNonNull(domainId, DOMAIN_ID_CAN_NOT_BE_NULL);
        Set<GlobalTransactionId> ids = domainIdToPreparedGtrids.get(domainId);
        if(null != ids)
        {
            ids.removeIf(id -> id.equals(globalTransactionId));
        }
    }

    public void removeAllGtridsFor(DomainId domainId)
    {
        Objects.requireNonNull(domainId, DOMAIN_ID_CAN_NOT_BE_NULL);
        domainIdToPreparedGtrids.remove(domainId);
    }

    public Set<GlobalTransactionId> getPreparedGtrids()
    {
        return Collections.unmodifiableSet(domainIdToPreparedGtrids.values().stream()
                                                                   .flatMap(Set::stream)
                                                                   .collect(Collectors.toSet()));
    }

    public Map<DomainId, Set<GlobalTransactionId>> getDomainIdToPreparedGtrids()
    {
        return Collections.unmodifiableMap(domainIdToPreparedGtrids);
    }

}
