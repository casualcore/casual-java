package se.kodarkatten.casual.network.messages.transaction;

import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;

import javax.transaction.xa.Xid;
import java.util.UUID;
/**
 * Created by aleph on 2017-04-03.
 */
public final class CasualTransactionResourcePrepareReplyMessage extends AbstractCasualTransactionReplyMessage
{
    private CasualTransactionResourcePrepareReplyMessage(final UUID execution, final Xid xid, int resourceId, final XAReturnCode transactionReturnCode)
    {
        super(execution, xid, resourceId, transactionReturnCode);
    }

    public static CasualTransactionResourcePrepareReplyMessage of(final UUID execution, final Xid xid, int resourceId, final XAReturnCode transactionReturnCode)
    {
        return new CasualTransactionResourcePrepareReplyMessage(execution, XID.of(xid), resourceId, transactionReturnCode);
    }

    @Override
    protected CasualNWMessageType getTransactionType()
    {
        return CasualNWMessageType.PREPARE_REQUEST_REPLY;
    }
}


