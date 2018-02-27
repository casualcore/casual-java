package se.laz.casual.network.outbound

import se.kodarkatten.casual.api.buffer.CasualBuffer
import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.flags.ErrorState
import se.kodarkatten.casual.api.flags.TransactionState
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer
import se.laz.casual.network.outbound.CasualMessageHandler
import se.laz.casual.network.outbound.CorrelatorImpl
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

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
        CompletableFuture<CasualNWMessage<CasualNWMessage<CasualServiceCallReplyMessage>>> f = new CompletableFuture<>()
        CasualNWMessage<CasualServiceCallReplyMessage> msg = createReplyMessage()
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
