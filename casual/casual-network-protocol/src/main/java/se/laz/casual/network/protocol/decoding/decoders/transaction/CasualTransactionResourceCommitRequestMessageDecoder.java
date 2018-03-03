/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.transaction;

import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.XAFlags;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;

import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
public final class CasualTransactionResourceCommitRequestMessageDecoder extends AbstractCasualTransactionRequestDecoder<CasualTransactionResourceCommitRequestMessage>
{
    private CasualTransactionResourceCommitRequestMessageDecoder()
    {}

    public static CasualTransactionResourceCommitRequestMessageDecoder of()
    {
        return new CasualTransactionResourceCommitRequestMessageDecoder();
    }

    @Override
    protected CasualTransactionResourceCommitRequestMessage createTransactionRequestMessage(final UUID execution, final Xid xid, int resourceId, final Flag<XAFlags> flags)
    {
        return CasualTransactionResourceCommitRequestMessage.of(execution, xid, resourceId, flags);
    }
}
