package se.kodarkatten.casual.network.messages.transaction;

import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.XAFlags;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;

import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
public final class CasualTransactionResourceRollbackRequestMessage extends AbstractCasualTransactionRequestMessage
{
    protected CasualTransactionResourceRollbackRequestMessage(final UUID execution, final Xid xid, long resourceId, long flags)
    {
        super(execution, xid, resourceId, flags);
    }

    public static CasualTransactionResourceRollbackRequestMessage of(final UUID execution, final Xid xid, long resourceId, final Flag<XAFlags> flags)
    {
        return new CasualTransactionResourceRollbackRequestMessage(execution, XID.of(xid), resourceId, flags.getFlagValue());
    }

    @Override
    protected CasualNWMessageType getTransactionType()
    {
        return CasualNWMessageType.REQUEST_ROLLBACK;
    }
}
