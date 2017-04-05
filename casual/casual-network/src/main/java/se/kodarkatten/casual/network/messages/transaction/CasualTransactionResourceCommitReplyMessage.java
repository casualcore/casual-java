package se.kodarkatten.casual.network.messages.transaction;

import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;

import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
public final class CasualTransactionResourceCommitReplyMessage extends AbstractCasualTransactionReplyMessage
{
    private CasualTransactionResourceCommitReplyMessage(final UUID execution, final Xid xid, long resourceId, final XAReturnCode transactionReturnCode)
    {
        super(execution, xid, resourceId, transactionReturnCode);
    }

    public static CasualTransactionResourceCommitReplyMessage of(final UUID execution, final Xid xid, long resourceId, final XAReturnCode transactionReturnCode)
    {
        return new CasualTransactionResourceCommitReplyMessage(execution, XID.of(xid), resourceId, transactionReturnCode);
    }

    @Override
    protected CasualNWMessageType getTransactionType()
    {
        return CasualNWMessageType.COMMIT_REQUEST_REPLY;
    }
}
