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
public final class CasualTransactionResourceCommitReplyMessage extends AbstractCasualTransactionReplyMessage
{
    private CasualTransactionResourceCommitReplyMessage(final UUID execution, final Xid xid, int resourceId, final XAReturnCode transactionReturnCode)
    {
        super(execution, xid, resourceId, transactionReturnCode);
    }

    public static CasualTransactionResourceCommitReplyMessage of(final UUID execution, final Xid xid, int resourceId, final XAReturnCode transactionReturnCode)
    {
        return new CasualTransactionResourceCommitReplyMessage(execution, XID.of(xid), resourceId, transactionReturnCode);
    }

    @Override
    protected CasualNWMessageType getTransactionType()
    {
        return CasualNWMessageType.COMMIT_REQUEST_REPLY;
    }
}
