/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;


import javax.transaction.xa.Xid;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

// java:S6548 - yes it is intentional
@SuppressWarnings("java:S6548")
public final class CasualResourceManager
{
    private static final CasualResourceManager INSTANCE = new CasualResourceManager();
    private final AtomicInteger currentRMId = new AtomicInteger(1);
    private final ConcurrentMap<Xid, Boolean> pendingRequests = new ConcurrentHashMap<>();
    private CasualResourceManager()
    {}

    public static final CasualResourceManager getInstance()
    {
        return INSTANCE;
    }

    public final Integer getNextId()
    {
        return currentRMId.getAndIncrement();
    }

    public void put(final Xid xid)
    {
        if(pendingRequests.containsKey(xid))
        {
            throw new CasualResourceAdapterException("xid: " + xid + " already stored");
        }
        pendingRequests.put(xid, true);
    }

    public void remove(final Xid xid)
    {
        pendingRequests.remove(xid);
    }

    public boolean isPending(final Xid xid)
    {
        return pendingRequests.containsKey(xid);
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
