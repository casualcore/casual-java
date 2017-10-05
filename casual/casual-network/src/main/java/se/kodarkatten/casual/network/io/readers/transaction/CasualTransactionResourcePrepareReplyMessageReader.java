package se.kodarkatten.casual.network.io.readers.transaction;


import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourcePrepareReplyMessage;
import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
public class CasualTransactionResourcePrepareReplyMessageReader extends AbstractCasualTransactionReplyReader<CasualTransactionResourcePrepareReplyMessage>
{
    private CasualTransactionResourcePrepareReplyMessageReader()
    {}

    public static CasualTransactionResourcePrepareReplyMessageReader of()
    {
        return new CasualTransactionResourcePrepareReplyMessageReader();
    }

    @Override
    protected CasualTransactionResourcePrepareReplyMessage createTransactionReplyMessage(final UUID execution, final Xid xid, int resourceId, final XAReturnCode r)
    {
        return CasualTransactionResourcePrepareReplyMessage.of(execution, xid, resourceId, r);
    }
}
