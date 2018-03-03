/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.transaction;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.xa.XAReturnCode;
import se.laz.casual.api.xa.XID;

import javax.transaction.xa.Xid;
import java.util.UUID;
/**
 * Created by aleph on 2017-04-03.
 */
public final class CasualTransactionResourcePrepareReplyMessage extends AbstractCasualTransactionReplyMessage
{
    private CasualTransactionResourcePrepareReplyMessage(final UUID execution, final Xid xid, int resourceId, final XAReturnCode transactionReturnCode)
    {
        super(execution, xid, resourceId, transactionReturnCode);
    }

    public static CasualTransactionResourcePrepareReplyMessage of(final UUID execution, final Xid xid, int resourceId, final XAReturnCode transactionReturnCode)
    {
        return new CasualTransactionResourcePrepareReplyMessage(execution, XID.of(xid), resourceId, transactionReturnCode);
    }

    @Override
    protected CasualNWMessageType getTransactionType()
    {
        return CasualNWMessageType.PREPARE_REQUEST_REPLY;
    }
}