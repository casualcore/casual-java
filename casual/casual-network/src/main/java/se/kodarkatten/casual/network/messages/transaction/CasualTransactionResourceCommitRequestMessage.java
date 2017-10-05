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
public final class CasualTransactionResourceCommitRequestMessage extends AbstractCasualTransactionRequestMessage
{
    protected CasualTransactionResourceCommitRequestMessage(final UUID execution, final Xid xid, int resourceId, int flags)
    {
        super(execution, xid, resourceId, flags);
    }

    public static CasualTransactionResourceCommitRequestMessage of(final UUID execution, final Xid xid, int resourceId, final Flag<XAFlags> flags)
    {
        return new CasualTransactionResourceCommitRequestMessage(execution, XID.of(xid), resourceId, flags.getFlagValue());
    }

    @Override
    protected CasualNWMessageType getTransactionType()
    {
        return CasualNWMessageType.COMMIT_REQUEST;
    }
}
