package se.kodarkatten.casual.network.io.readers.transaction;

import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.XAFlags;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
public final class CasualTransactionResourceRollbackRequestMessageReader extends AbstractCasualTransactionRequestReader<CasualTransactionResourceRollbackRequestMessage>
{
    private CasualTransactionResourceRollbackRequestMessageReader()
    {}

    public static CasualTransactionResourceRollbackRequestMessageReader of()
    {
        return new CasualTransactionResourceRollbackRequestMessageReader();
    }

    @Override
    protected CasualTransactionResourceRollbackRequestMessage createTransactionRequestMessage(final UUID execution, final Xid xid, int resourceId, final Flag<XAFlags> flags)
    {
        return CasualTransactionResourceRollbackRequestMessage.of(execution, xid, resourceId, flags);
    }
}
