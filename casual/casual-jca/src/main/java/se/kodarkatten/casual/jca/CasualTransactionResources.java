package se.kodarkatten.casual.jca;

import javax.transaction.xa.Xid;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author jone
 */
public final class CasualTransactionResources
{
    private static final CasualTransactionResources INSTANCE = new CasualTransactionResources();

    private CasualTransactionResources()
    {
    }

    public static final CasualTransactionResources getInstance()
    {
        return INSTANCE;
    }

    private final ConcurrentHashMap<Xid, Long> pendingCommitRequests = new ConcurrentHashMap<>();

    public final Long getResourceIdForXid(final Xid xid)
    {
        Long rId = pendingCommitRequests.get(xid);
        if(null == rId)
        {
            rId = ThreadLocalRandom.current().nextLong();
            Long found = addResourceIdForXid(rId, xid);
            rId = ( found == null ) ? rId : found;
        }
        return rId;
    }

    private final Long addResourceIdForXid(final Long resourceId, final Xid xid)
    {
        return pendingCommitRequests.putIfAbsent(xid, resourceId);
    }

    public final void removeResourceIdForXid(final Xid xid)
    {
        pendingCommitRequests.remove(xid);
    }

    public final boolean xidPending(final Xid xid)
    {
        return pendingCommitRequests.containsKey(xid);
    }

    @Override
    public String toString()
    {
        return "CasualTransactionResources{" +
                "pendingCommitRequests=" + pendingCommitRequests +
                '}';
    }
}
