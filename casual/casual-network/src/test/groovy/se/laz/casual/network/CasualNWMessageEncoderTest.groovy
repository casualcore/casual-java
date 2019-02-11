/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.api.xa.XID
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import se.laz.casual.api.buffer.type.ServiceBuffer
import spock.lang.Shared
import spock.lang.Specification

class CasualNWMessageEncoderTest extends Specification
{
    @Shared
    UUID corrid = UUID.randomUUID()

    def 'ok msg'()
    {
        setup:
        CasualNWMessage<CasualServiceCallReplyMessage> msg = createReplyMessage()
        ByteBuf buf = Unpooled.buffer()
        for(byte[] b : msg.toNetworkBytes())
        {
            buf.writeBytes(b)
        }
        def encoder = CasualNWMessageEncoder.of()
        ByteBuf out = Unpooled.buffer()
        when:
        encoder.encode(null, msg, out)
        then:
        noExceptionThrown()
        buf == out
    }

    def createReplyMessage()
    {
        CasualBuffer msg = JsonBuffer.of('{"msg": "hello world"}')
        CasualServiceCallReplyMessage message = CasualServiceCallReplyMessage.createBuilder()
                                                                             .setTransactionState(TransactionState.TX_ACTIVE)
                                                                             .setUserSuppliedError(0)
                                                                             .setError(ErrorState.OK)
                                                                             .setXid(XID.NULL_XID)
                                                                             .setExecution(UUID.randomUUID())
                                                                             .setServiceBuffer(ServiceBuffer.of(msg))
                                                                             .build()
        return CasualNWMessageImpl.of(corrid, message)
    }

}
