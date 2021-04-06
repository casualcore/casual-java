/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import com.google.protobuf.ByteString
import org.junit.Ignore
import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.xa.XID
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualReply
import se.laz.casual.network.messages.CasualServiceCallReply
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

@Ignore
class CasualMessageHandlerTest extends Specification
{
    @Shared
    def correlator = CorrelatorImpl.of()
    @Shared
    def corrid = UUID.randomUUID()
    @Shared
    def instance = CasualMessageHandler.of(correlator)

    def 'failed construction'()
    {
        when:
        CasualMessageHandler h = CasualMessageHandler.of(null)
        then:
        thrown(NullPointerException)
    }

    def 'ok'()
    {
        setup:
        CompletableFuture<CasualReply> f = new CompletableFuture<>()
        CasualReply msg = createReplyMessage()
        correlator.put(corrid, f)
        when:
        instance.channelRead0(null, msg)
        def r = f.get()
        then:
        noExceptionThrown()
        r == msg
    }

    def createReplyMessage()
    {
        CasualBuffer msg = JsonBuffer.of('{"msg": "hello world"}')
        CasualServiceCallReply message = CasualServiceCallReply.newBuilder()
        .setTransactionState(MessageCreator.toTransactionState(TransactionState.TX_ACTIVE))
        .setUser(0)
        .setResult(se.laz.casual.network.messages.ErrorState.valueOf(ErrorState.OK.name()))
        .setXid(MessageCreator.toXID(XID.NULL_XID))
        .setExecution(MessageCreator.toUUID4(UUID.randomUUID()))
        .setBufferTypeName(msg.getType())
        .setPayload(ByteString.copyFrom(msg.getBytes().get(0)))
        .build()

        return CasualReply.newBuilder()
                .setMessageType(CasualReply.MessageType.SERVICE_CALL_REPLY)
                .setCorrelationId(MessageCreator.toUUID4(corrid))
                .setServiceCall(message)
                .build()
    }
}
