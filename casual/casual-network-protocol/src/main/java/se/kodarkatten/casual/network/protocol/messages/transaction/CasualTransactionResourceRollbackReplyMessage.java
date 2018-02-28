package se.kodarkatten.casual.network.protocol.messages.transaction;

import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessageType;

import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
public class CasualTransactionResourceRollbackReplyMessage extends AbstractCasualTransactionReplyMessage
{
    protected CasualTransactionResourceRollbackReplyMessage(final UUID execution, final Xid xid, int resourceId, final XAReturnCode xaReturnCode)
    {
        super(execution, xid, resourceId, xaReturnCode);
    }

    public static CasualTransactionResourceRollbackReplyMessage of(final UUID execution, final Xid xid, int resourceId, final XAReturnCode xaReturnCode)
    {
        return new CasualTransactionResourceRollbackReplyMessage(execution, XID.of(xid), resourceId, xaReturnCode);
    }

    @Override
    protected CasualNWMessageType getTransactionType()
    {
        return CasualNWMessageType.REQUEST_ROLLBACK_REPLY;
    }
}