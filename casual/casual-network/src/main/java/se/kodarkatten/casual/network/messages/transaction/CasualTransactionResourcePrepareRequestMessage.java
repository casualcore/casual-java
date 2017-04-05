package se.kodarkatten.casual.network.messages.transaction;

import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.XAFlags;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;

import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-30.
 */
public final class CasualTransactionResourcePrepareRequestMessage extends AbstractCasualTransactionRequestMessage
{
    private CasualTransactionResourcePrepareRequestMessage(final UUID execution, final Xid xid, long resourceId, long flags)
    {
        super(execution, xid, resourceId, flags);
    }

    public static CasualTransactionResourcePrepareRequestMessage of(final UUID execution, final Xid xid, long resourceId, final Flag<XAFlags> flags)
    {
        return new CasualTransactionResourcePrepareRequestMessage(execution, XID.of(xid), resourceId, flags.getFlagValue());
    }


    @Override
    protected CasualNWMessageType getTransactionType()
    {
        return CasualNWMessageType.PREPARE_REQUEST;
    }

}
