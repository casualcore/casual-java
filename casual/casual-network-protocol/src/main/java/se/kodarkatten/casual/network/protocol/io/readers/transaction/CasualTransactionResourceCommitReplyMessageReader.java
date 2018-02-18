package se.kodarkatten.casual.network.protocol.io.readers.transaction;

import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitReplyMessage;

import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
public class CasualTransactionResourceCommitReplyMessageReader extends AbstractCasualTransactionReplyReader<CasualTransactionResourceCommitReplyMessage>
{
    private CasualTransactionResourceCommitReplyMessageReader()
    {}

    public static CasualTransactionResourceCommitReplyMessageReader of()
    {
        return new CasualTransactionResourceCommitReplyMessageReader();
    }

    @Override
    protected CasualTransactionResourceCommitReplyMessage createTransactionReplyMessage(final UUID execution, final Xid xid, int resourceId, final XAReturnCode r)
    {
        return CasualTransactionResourceCommitReplyMessage.of(execution, xid, resourceId, r);
    }
}
