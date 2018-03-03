/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.transaction;

import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.XAFlags;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.xa.XID;

import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-30.
 */
public final class CasualTransactionResourcePrepareRequestMessage extends AbstractCasualTransactionRequestMessage
{
    private CasualTransactionResourcePrepareRequestMessage(final UUID execution, final Xid xid, int resourceId, int flags)
    {
        super(execution, xid, resourceId, flags);
    }

    public static CasualTransactionResourcePrepareRequestMessage of(final UUID execution, final Xid xid, int resourceId, final Flag<XAFlags> flags)
    {
        return new CasualTransactionResourcePrepareRequestMessage(execution, XID.of(xid), resourceId, flags.getFlagValue());
    }


    @Override
    protected CasualNWMessageType getTransactionType()
    {
        return CasualNWMessageType.PREPARE_REQUEST;
    }

}
