/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.transaction;

import se.laz.casual.api.xa.XAReturnCode;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitReplyMessage;

import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
public class CasualTransactionResourceCommitReplyMessageDecoder extends AbstractCasualTransactionReplyDecoder<CasualTransactionResourceCommitReplyMessage>
{
    private CasualTransactionResourceCommitReplyMessageDecoder()
    {}

    public static CasualTransactionResourceCommitReplyMessageDecoder of()
    {
        return new CasualTransactionResourceCommitReplyMessageDecoder();
    }

    @Override
    protected CasualTransactionResourceCommitReplyMessage createTransactionReplyMessage(final UUID execution, final Xid xid, int resourceId, final XAReturnCode r)
    {
        return CasualTransactionResourceCommitReplyMessage.of(execution, xid, resourceId, r);
    }
}
