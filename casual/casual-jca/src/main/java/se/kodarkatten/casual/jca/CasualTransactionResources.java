package se.kodarkatten.casual.jca;

import javax.transaction.xa.Xid;
import java.util.Map;
import java.util.Optional;
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

    private final Map<Xid, Long> pendingCommitRequests = new ConcurrentHashMap<>();

    public final Long getResourceIdForXid(final Xid xid)
    {
        Long rId = pendingCommitRequests.get(xid);
        if(null == rId)
        {
            rId = ThreadLocalRandom.current().nextLong();
            addResourceIdForXid(rId, xid);
        }
        return rId;
    }

    public final void addResourceIdForXid(final Long resourceId, final Xid xid)
    {
        pendingCommitRequests.put(xid, resourceId);
    }

    public final void removeResourceIdForXid(final Xid xid)
    {
        pendingCommitRequests.remove(xid);
    }

    public final boolean xidPending(final Xid xid)
    {
        return pendingCommitRequests.containsKey(xid);
    }

}
