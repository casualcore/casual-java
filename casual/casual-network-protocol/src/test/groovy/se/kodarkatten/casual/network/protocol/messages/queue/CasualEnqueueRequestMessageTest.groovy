package se.kodarkatten.casual.network.protocol.messages.queue

import se.kodarkatten.casual.api.queue.QueueMessage
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.protocol.io.CasualNetworkReader
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer
import se.kodarkatten.casual.network.protocol.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel
import se.kodarkatten.casual.network.protocol.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification
import java.time.LocalDateTime

class CasualEnqueueRequestMessageTest extends Specification
{
    @Shared
    def serviceData
    @Shared
    def serviceType = 'application/json'
    @Shared
    def serviceBuffer
    @Shared
    def asyncSink
    @Shared
    def syncSink

    def setupSpec()
    {
        List<byte[]> l = new ArrayList<>()
        l.add('{"hello" : "world"}'.bytes)
        serviceData = l
        serviceBuffer = ServiceBuffer.of(serviceType, serviceData)
    }

    def setup()
    {
        asyncSink = new LocalAsyncByteChannel()
        syncSink = new LocalByteChannel()
    }

    def cleanup()
    {
        asyncSink = null
        syncSink = null
    }

    def "roundtrip"()
    {
        setup:
        EnqueueMessage enqueueMsg = EnqueueMessage.of(QueueMessage.createBuilder()
                                                       .withId(UUID.randomUUID())
                                                       .withReplyQueue("qspace:qname")
                                                       .withCorrelationInformation("correlationInformation")
                                                       .withAvailableSince(LocalDateTime.now())
                                                       .withPayload(serviceBuffer)
                                                       .build())
        CasualEnqueueRequestMessage requestMsg = CasualEnqueueRequestMessage.createBuilder()
                                                    .withExecution(UUID.randomUUID())
                                                    .withQueueName("best.queue.ever")
                                                    .withXid(XID.NULL_XID)
                                                    .withMessage(enqueueMsg)
                                                    .build()
        CasualNWMessageImpl<CasualEnqueueRequestMessage> msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessageImpl<CasualEnqueueRequestMessage> asyncResurrectedMsg = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessageImpl<CasualEnqueueRequestMessage> syncResurrectedMsg = TestUtils.roundtripMessage(msg, syncSink)
        then:
        networkBytes != null
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg

        EnqueueMessage m = msg.getMessage().getMessage()
        EnqueueMessage am = msg.getMessage().getMessage()
        EnqueueMessage sm = msg.getMessage().getMessage()
        Arrays.deepEquals( m.getPayload().getPayload().toArray(), am.getPayload().getPayload().toArray( ) )
        Arrays.deepEquals( m.getPayload().getPayload().toArray(), sm.getPayload().getPayload().toArray( ) )

    }

    def "roundtrip - force chunking"()
    {
        setup:
        EnqueueMessage enqueueMsg = EnqueueMessage.of(QueueMessage.createBuilder()
                                                       .withId(UUID.randomUUID())
                                                       .withReplyQueue("qspace:qname")
                                                       .withCorrelationInformation("correlationInformation")
                                                       .withAvailableSince(LocalDateTime.now())
                                                       .withPayload(serviceBuffer)
                                                       .build())
        CasualEnqueueRequestMessage requestMsg = CasualEnqueueRequestMessage.createBuilder()
                .withExecution(UUID.randomUUID())
                .withQueueName("best.queue.ever")
                .withXid(XID.NULL_XID)
                .withMessage(enqueueMsg)
                .build()
        CasualNWMessageImpl<CasualEnqueueRequestMessage> msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessageImpl<CasualEnqueueRequestMessage> asyncResurrectedMsg  = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessageImpl<CasualEnqueueRequestMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        then:
        networkBytes != null
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg

        EnqueueMessage m = msg.getMessage().getMessage()
        EnqueueMessage am = msg.getMessage().getMessage()
        EnqueueMessage sm = msg.getMessage().getMessage()
        Arrays.deepEquals( m.getPayload().getPayload().toArray(), am.getPayload().getPayload().toArray( ) )
        Arrays.deepEquals( m.getPayload().getPayload().toArray(), sm.getPayload().getPayload().toArray( ) )
    }

}
