/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.transaction;

import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.XAFlags;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;

import javax.transaction.xa.Xid;
import java.util.UUID;


/**
 * Created by aleph on 2017-03-31.
 */
public final class CasualTransactionResourcePrepareRequestMessageDecoder extends AbstractCasualTransactionRequestDecoder<CasualTransactionResourcePrepareRequestMessage>
{
    private CasualTransactionResourcePrepareRequestMessageDecoder()
    {}

    public static CasualTransactionResourcePrepareRequestMessageDecoder of()
    {
        return new CasualTransactionResourcePrepareRequestMessageDecoder();
    }

    @Override
    protected CasualTransactionResourcePrepareRequestMessage  createTransactionRequestMessage(final UUID execution, final Xid xid, int resourceId, final Flag<XAFlags> flags)
    {
        return CasualTransactionResourcePrepareRequestMessage.of(execution, xid, resourceId, flags);
    }
}
