/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;


import javax.transaction.xa.Xid;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

// java:S6548 - yes it is intentional
@SuppressWarnings("java:S6548")
public final class CasualResourceManager
{
    private static final CasualResourceManager INSTANCE = new CasualResourceManager();
    private static final Set<Xid> EMPTY_SET = Collections.emptySet();
    private final AtomicInteger currentRMId = new AtomicInteger(1);
    private final ConcurrentMap<DomainId, Set<Xid>> pendingRequests = new ConcurrentHashMap<>();
    private CasualResourceManager()
    {}

    public static CasualResourceManager getInstance()
    {
        return INSTANCE;
    }

    public Integer getNextId()
    {
        return currentRMId.getAndIncrement();
    }

    public synchronized void put(DomainId domainId, final Xid xid)
    {
        if(pendingRequests.getOrDefault(domainId, EMPTY_SET).contains(xid))
        {
            throw new CasualResourceAdapterException("xid: " + xid + " already stored for domain: " + domainId);
        }
        pendingRequests.computeIfAbsent(domainId, k -> ConcurrentHashMap.newKeySet()).add(xid);
    }

    public synchronized void remove(DomainId domainId, final Xid xid)
    {
        Set<Xid> inFlight = pendingRequests.get(domainId);
        if(inFlight != null)
        {
            inFlight.remove(xid);
            if(inFlight.isEmpty())
            {
                pendingRequests.remove(domainId);
            }
        }
    }

    public boolean isPending(DomainId domainId, final Xid xid)
    {
        return pendingRequests.getOrDefault(domainId, EMPTY_SET).contains(xid);
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
