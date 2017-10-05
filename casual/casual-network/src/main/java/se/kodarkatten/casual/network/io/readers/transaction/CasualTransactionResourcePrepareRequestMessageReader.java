package se.kodarkatten.casual.network.io.readers.transaction;

import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.XAFlags;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourcePrepareRequestMessage;

import javax.transaction.xa.Xid;
import java.util.UUID;


/**
 * Created by aleph on 2017-03-31.
 */
public final class CasualTransactionResourcePrepareRequestMessageReader extends AbstractCasualTransactionRequestReader<CasualTransactionResourcePrepareRequestMessage>
{
    private CasualTransactionResourcePrepareRequestMessageReader()
    {}

    public static CasualTransactionResourcePrepareRequestMessageReader of()
    {
        return new CasualTransactionResourcePrepareRequestMessageReader();
    }

    @Override
    protected CasualTransactionResourcePrepareRequestMessage  createTransactionRequestMessage(final UUID execution, final Xid xid, int resourceId, final Flag<XAFlags> flags)
    {
        return CasualTransactionResourcePrepareRequestMessage.of(execution, xid, resourceId, flags);
    }
}
