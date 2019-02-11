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

class CasualNWMessageDecoderTest extends Specification
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
        def decoder = CasualNWMessageDecoder.of()
        List<Object> out = new ArrayList<>()
        when:
        decoder.decode(null, buf, out)
        decoder.decode(null, buf, out)
        CasualNWMessage<CasualServiceCallReplyMessage> r = (CasualNWMessage<CasualServiceCallReplyMessage> )out.get(0)
        then:
        noExceptionThrown()
        r == msg
    }

    def 'crap body'()
    {
        setup:
        CasualNWMessage<CasualServiceCallReplyMessage> msg = createReplyMessage()
        ByteBuf buf = Unpooled.buffer()
        for(byte[] b : msg.toNetworkBytes())
        {
            buf.writeBytes(b)
        }
        def decoder = CasualNWMessageDecoder.of()
        List<Object> out = new ArrayList<>()
        ByteBuf crapBody = Unpooled.buffer()
        when:
        decoder.decode(null, buf, out)
        for(int i = 0; i< decoder.header.payloadSize; ++i)
        {
            crapBody.writeByte(0)
        }
        decoder.decode(null, crapBody, out)
        then:
        thrown(CasualDecoderException)
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
