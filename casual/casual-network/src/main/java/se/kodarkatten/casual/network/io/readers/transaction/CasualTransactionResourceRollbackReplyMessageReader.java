package se.kodarkatten.casual.network.io.readers.transaction;

import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceRollbackReplyMessage;

import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
public final class CasualTransactionResourceRollbackReplyMessageReader extends AbstractCasualTransactionReplyReader<CasualTransactionResourceRollbackReplyMessage>
{
    private CasualTransactionResourceRollbackReplyMessageReader()
    {}

    public static CasualTransactionResourceRollbackReplyMessageReader of()
    {
        return new CasualTransactionResourceRollbackReplyMessageReader();
    }

    @Override
    protected CasualTransactionResourceRollbackReplyMessage createTransactionReplyMessage(final UUID execution, final Xid xid, int resourceId, final XAReturnCode r)
    {
        return CasualTransactionResourceRollbackReplyMessage.of(execution, xid, resourceId, r);
    }
}
