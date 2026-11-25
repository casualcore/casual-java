/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.transaction.xa.Xid;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

// java:S6548 - singleton is intentional
@SuppressWarnings("java:S6548")
public final class CasualResourceManager
{
    private static final CasualResourceManager INSTANCE = new CasualResourceManager();
    private static final Set<Xid> EMPTY_SET = Collections.emptySet();
    private static final String RESOURCE_MANAGER_ID_MUST_NOT_BE_NULL = "resourceManagerId must not be null";
    private final AtomicInteger currentRMId = new AtomicInteger(1);
    private final ConcurrentMap<ResourceId, Set<Xid>> pendingRequests = new ConcurrentHashMap<>();
    private CasualResourceManager()
    {}

    // Exposed internal representation is expected.
    @SuppressFBWarnings("MS_EXPOSE_REP")
    public static CasualResourceManager getInstance()
    {
        return INSTANCE;
    }

    public Integer getNextId()
    {
        return currentRMId.getAndIncrement();
    }

    /**
     * Registers a pending outbound transaction for the given resource manager.
     *
     * @param resourceId the resource manager identifier
     * @param xid the transaction identifier
     * @throws NullPointerException if {@code resourceManagerId} or {@code xid} is {@code null}
     * @throws CasualResourceAdapterException if {@code xid} is already registered for {@code resourceManagerId}
     */
    public synchronized void put(ResourceId resourceId, final Xid xid)
    {
        Objects.requireNonNull(resourceId, RESOURCE_MANAGER_ID_MUST_NOT_BE_NULL);
        Objects.requireNonNull(xid, "xid must not be null");
        if(pendingRequests.getOrDefault(resourceId, EMPTY_SET).contains(xid))
        {
            throw new CasualResourceAdapterException("xid: " + xid + " already stored for domain: " + resourceId);
        }
        pendingRequests.computeIfAbsent(resourceId, k -> ConcurrentHashMap.newKeySet()).add(xid);
    }

    public synchronized boolean hasPending()
    {
        return !pendingRequests.isEmpty();
    }

    /**
     * Returns the number of pending outbound transaction entries across resource managers.
     *
     * <p>This method synchronizes with registration and removal. An XID registered
     * at multiple resource managers contributes one entry per resource manager.
     *
     * @return the number of pending outbound transaction entries
     */
    public synchronized long getPendingTransactionCount()
    {
        return pendingRequests.values().stream().mapToLong(Set::size).sum();
    }

    /**
     * Removes a pending outbound transaction for the given resource manager.
     *
     * @param resourceId the resource manager identifier
     * @param xid the transaction identifier
     * @throws NullPointerException if {@code resourceManagerId} is {@code null}
     */
    public synchronized void remove(ResourceId resourceId, final Xid xid)
    {
        Objects.requireNonNull(resourceId, RESOURCE_MANAGER_ID_MUST_NOT_BE_NULL);
        Set<Xid> inFlight = pendingRequests.get(resourceId);
        if(inFlight != null)
        {
            inFlight.remove(xid);
            if(inFlight.isEmpty())
            {
                pendingRequests.remove(resourceId);
            }
        }
    }

    /**
     * Returns whether the transaction is pending for the given resource manager.
     *
     * @param resourceId the resource manager identifier
     * @param xid the transaction identifier
     * @return {@code true} if {@code xid} is pending for {@code resourceManagerId}
     * @throws NullPointerException if {@code resourceManagerId} is {@code null}
     */
    public boolean isPending(ResourceId resourceId, final Xid xid)
    {
        Objects.requireNonNull(resourceId, RESOURCE_MANAGER_ID_MUST_NOT_BE_NULL);
        return pendingRequests.getOrDefault(resourceId, EMPTY_SET).contains(xid);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualResourceManager{");
        sb.append("currentRMId=").append(currentRMId);
        sb.append('}');
        return sb.toString();
    }
}
