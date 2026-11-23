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
    private final AtomicInteger currentRMId = new AtomicInteger(1);
    private final ConcurrentMap<ResourceManagerId, Set<Xid>> pendingRequests = new ConcurrentHashMap<>();
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

    public synchronized void put(Address domainId, final Xid xid)
    {
        putInternal(ResourceManagerId.of(domainId), domainId, xid);
    }

    /**
     * Registers a pending outbound transaction for the given resource manager.
     *
     * @param resourceManagerId the resource manager identifier
     * @param xid the transaction identifier
     * @throws NullPointerException if {@code resourceManagerId} or {@code xid} is {@code null}
     * @throws CasualResourceAdapterException if {@code xid} is already registered for {@code resourceManagerId}
     */
    public synchronized void put(ResourceManagerId resourceManagerId, final Xid xid)
    {
        Objects.requireNonNull(resourceManagerId, "resourceManagerId must not be null");
        putInternal(resourceManagerId, resourceManagerId, xid);
    }

    private void putInternal(ResourceManagerId key, Object displayId, final Xid xid)
    {
        Objects.requireNonNull(xid, "xid must not be null");
        if(pendingRequests.getOrDefault(key, EMPTY_SET).contains(xid))
        {
            throw new CasualResourceAdapterException("xid: " + xid + " already stored for domain: " + displayId);
        }
        pendingRequests.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(xid);
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

    public synchronized void remove(Address domainId, final Xid xid)
    {
        remove(ResourceManagerId.of(domainId), xid);
    }

    /**
     * Removes a pending outbound transaction for the given resource manager.
     *
     * @param resourceManagerId the resource manager identifier
     * @param xid the transaction identifier
     * @throws NullPointerException if {@code resourceManagerId} is {@code null}
     */
    public synchronized void remove(ResourceManagerId resourceManagerId, final Xid xid)
    {
        Objects.requireNonNull(resourceManagerId, "resourceManagerId must not be null");
        Set<Xid> inFlight = pendingRequests.get(resourceManagerId);
        if(inFlight != null)
        {
            inFlight.remove(xid);
            if(inFlight.isEmpty())
            {
                pendingRequests.remove(resourceManagerId);
            }
        }
    }

    public boolean isPending(Address domainId, final Xid xid)
    {
        return isPending(ResourceManagerId.of(domainId), xid);
    }

    /**
     * Returns whether the transaction is pending for the given resource manager.
     *
     * @param resourceManagerId the resource manager identifier
     * @param xid the transaction identifier
     * @return {@code true} if {@code xid} is pending for {@code resourceManagerId}
     * @throws NullPointerException if {@code resourceManagerId} is {@code null}
     */
    public boolean isPending(ResourceManagerId resourceManagerId, final Xid xid)
    {
        Objects.requireNonNull(resourceManagerId, "resourceManagerId must not be null");
        return pendingRequests.getOrDefault(resourceManagerId, EMPTY_SET).contains(xid);
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
