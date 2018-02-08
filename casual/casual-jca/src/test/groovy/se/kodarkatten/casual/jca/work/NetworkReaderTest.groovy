package se.kodarkatten.casual.jca.work

import se.kodarkatten.casual.api.buffer.CasualBuffer
import se.kodarkatten.casual.api.buffer.ServiceReturn
import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.flags.ErrorState
import se.kodarkatten.casual.api.flags.ServiceReturnState
import se.kodarkatten.casual.api.flags.TransactionState
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.jca.ManagedConnectionInvalidator
import se.kodarkatten.casual.jca.message.Correlator
import se.kodarkatten.casual.jca.message.impl.CorrelatorImpl
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader
import se.kodarkatten.casual.network.messages.CasualNWMessageType
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage
import se.kodarkatten.casual.network.messages.service.ServiceBuffer
import se.kodarkatten.casual.network.utils.DummyWorkManager
import se.kodarkatten.casual.network.utils.LocalEchoSocketChannel
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

class NetworkReaderTest extends Specification
{
    @Shared
    CorrelatorImpl correlator
    @Shared
    LocalEchoSocketChannel socketChannel
    @Shared
    def invalidator
    @Shared
    NetworkReader instance
    @Shared
    DummyWorkManager workManager
    @Shared
    def payloadMsg = 'hello casual!'
    @Shared
    def payload = JsonBuffer.of("{\"msg\": \"${payloadMsg}\"}")
    @Shared
    def corrId = UUID.randomUUID()
    @Shared
    CasualNWMessage<CasualServiceCallReplyMessage> serviceRequestReplyMsg

    def setup()
    {
        correlator = CorrelatorImpl.of()
        socketChannel = LocalEchoSocketChannel.of()
        invalidator = Mock(ManagedConnectionInvalidator)
        instance = NetworkReader.of(correlator, socketChannel , invalidator)
        workManager = DummyWorkManager.of()
        serviceRequestReplyMsg = createRequestMessage()
    }

    def cleanup()
    {
        workManager.done()
    }

    def 'failed creation'()
    {
        when:
        NetworkReader.of(r, channel , inv)
        then:
        thrown(NullPointerException)
        where:
        r          | channel       | inv
        null       | socketChannel | invalidator
        correlator | null          | invalidator
        correlator | socketChannel | null
    }

    def 'ok request/response'()
    {
        setup:
        CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>> f = writeMsg(serviceRequestReplyMsg)
        when:
        workManager.startWork(instance)
        CasualNWMessage<CasualServiceCallReplyMessage> r = f.get()
        ServiceReturn<CasualBuffer> msg = toServiceReturn(r)
        JsonBuffer responseBuffer = JsonBuffer.of(msg.getReplyBuffer().getBytes())
        workManager.done()
        then:
        noExceptionThrown()
        r.getCorrelationId() == corrId
        responseBuffer == payload
        correlator.isEmpty()
    }

    def 'release'()
    {
        when:
        instance.release()
        then:
        instance.isReleased()
    }

    def 'fail read with IOException'()
    {
        setup:
        def localInvalidator = Mock(ManagedConnectionInvalidator)
        1 * localInvalidator.invalidate(_)
        def localInstance = NetworkReader.of(correlator, socketChannel , localInvalidator)
        socketChannel.setThrowIOExceptionOnRead()
        CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>> f = writeMsg(serviceRequestReplyMsg)
        when:
        workManager.startWork(localInstance)
        workManager.done()
        then:
        noExceptionThrown()
        localInstance.isReleased()
        f.isCompletedExceptionally()
    }

    def 'fail read with non IOException but header will be null'()
    {
        setup:
        def localInvalidator = Mock(ManagedConnectionInvalidator)
        def localInstance = NetworkReader.of(correlator, socketChannel , localInvalidator)
        socketChannel.setThrowRuntimeExceptionOnRead()
        CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>> f = writeMsg(serviceRequestReplyMsg)
        when:
        workManager.startWork(localInstance)
        workManager.done()
        then:
        noExceptionThrown()
        localInstance.isReleased()
        f.isCompletedExceptionally()
        1 * localInvalidator.invalidate(_)
    }

    def 'fail read with InterruptedException'()
    {
        setup:
        def localInvalidator = Mock(ManagedConnectionInvalidator)
        def localInstance = NetworkReader.of(correlator, socketChannel , localInvalidator)
        when:
        workManager.startWork(localInstance)
        Thread.currentThread().yield()
        workManager.interruptWork()
        workManager.done()
        then:
        noExceptionThrown()
        localInstance.isReleased()
        1 * localInvalidator.invalidate(_)
    }

    def 'fail read with non IOException - got header'()
    {
        setup:
        def localInvalidator = Mock(ManagedConnectionInvalidator)
        def localCorrelator = Mock(Correlator)
        def localInstance = NetworkReader.of(localCorrelator, socketChannel , localInvalidator)
        writeHeader(corrId, CasualNWMessageType.SERVICE_CALL_REPLY, 1)
        socketChannel.setThrowRuntimeExceptionOnNthRead(2)
        when:
        workManager.startWork(localInstance)
        workManager.done()
        then:
        noExceptionThrown()
        0 * localInvalidator.invalidate(_)
        1 * localCorrelator.completeExceptionally(*_) >> { args ->
            assert(args[0][0] == corrId)
            assert(args[1] instanceof RuntimeException)
            localInstance.release()
        }
    }

    def <X extends CasualNetworkTransmittable, T extends CasualNetworkTransmittable> CompletableFuture<CasualNWMessage<T>> writeMsg(final CasualNWMessage<X> msg)
    {
        CompletableFuture<CasualNWMessage<T>> f = new CompletableFuture<>()
        correlator.put(msg.getCorrelationId(), f)
        CasualNetworkWriter.write(socketChannel, msg)
        return f
    }

    def writeHeader(UUID corrid, CasualNWMessageType type, int payloadSize)
    {
        CasualNWMessageHeader header = CasualNWMessageHeader.createBuilder()
                .setCorrelationId(corrid)
                .setType(type)
                .setPayloadSize(payloadSize)
                .build()
        socketChannel.write(ByteBuffer.wrap(header.toNetworkBytes()))
    }


    def createRequestMessage()
    {
        CasualServiceCallReplyMessage message = CasualServiceCallReplyMessage.createBuilder()
                                                                                 .setExecution(UUID.randomUUID())
                                                                                 .setXid(XID.NULL_XID)
                                                                                 .setServiceBuffer(ServiceBuffer.of(payload))
                                                                                 .setError(ErrorState.OK)
                                                                                 .setUserSuppliedError(0)
                                                                                 .setTransactionState(TransactionState.TX_ACTIVE)
                                                                                 .build()
        CasualNWMessage<CasualServiceCallReplyMessage> m = CasualNWMessage.of( corrId, message )
        return m
    }

    ServiceReturn<CasualBuffer> toServiceReturn(CasualNWMessage<CasualServiceCallReplyMessage> v)
    {
        CasualServiceCallReplyMessage serviceReplyMessage = v.getMessage()
        return new ServiceReturn<>(serviceReplyMessage.getServiceBuffer(), (serviceReplyMessage.getError() == ErrorState.OK) ? ServiceReturnState.TPSUCCESS : ServiceReturnState.TPFAIL, serviceReplyMessage.getError(), serviceReplyMessage.getUserDefinedCode())
    }

}
