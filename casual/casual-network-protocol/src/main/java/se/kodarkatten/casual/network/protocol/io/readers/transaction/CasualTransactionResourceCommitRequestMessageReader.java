package se.kodarkatten.casual.network.protocol.io.readers.transaction;

import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.XAFlags;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;

import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
public final class CasualTransactionResourceCommitRequestMessageReader extends AbstractCasualTransactionRequestReader<CasualTransactionResourceCommitRequestMessage>
{
    private CasualTransactionResourceCommitRequestMessageReader()
    {}

    public static CasualTransactionResourceCommitRequestMessageReader of()
    {
        return new CasualTransactionResourceCommitRequestMessageReader();
    }

    @Override
    protected CasualTransactionResourceCommitRequestMessage createTransactionRequestMessage(final UUID execution, final Xid xid, int resourceId, final Flag<XAFlags> flags)
    {
        return CasualTransactionResourceCommitRequestMessage.of(execution, xid, resourceId, flags);
    }
}
