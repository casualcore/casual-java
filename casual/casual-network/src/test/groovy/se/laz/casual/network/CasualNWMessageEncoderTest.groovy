package se.laz.casual.network

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import se.kodarkatten.casual.api.buffer.CasualBuffer
import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.flags.ErrorState
import se.kodarkatten.casual.api.flags.TransactionState
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer
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
