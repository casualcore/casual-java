package se.kodarkatten.casual.network.messages.queue

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.service.ServiceBuffer
import se.kodarkatten.casual.network.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.utils.LocalByteChannel
import se.kodarkatten.casual.network.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.OffsetDateTime

class CasualDequeueReplyMessageTest extends Specification
{
    @Shared
    def serviceType = 'application/json'
    @Shared
    def asyncSink
    @Shared
    def syncSink

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
        def requestMsg = CasualDequeueReplyMessage.createBuilder()
                                                  .withExecution(UUID.randomUUID())
                                                  .withMessages(createMessages(5))
                                                  .build()
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessage<CasualDequeueReplyMessage> asyncResurrectedMsg  = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessage<CasualDequeueReplyMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink)
        then:
        networkBytes != null
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg
    }

    def "roundtrip - force chunking"()
    {
        setup:
        def requestMsg = CasualDequeueReplyMessage.createBuilder()
                .withExecution(UUID.randomUUID())
                .withMessages(createMessages(5))
                .build()
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualDequeueReplyMessage> asyncResurrectedMsg  = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessage<CasualDequeueReplyMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        then:
        networkBytes != null
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg
    }




    def createMessages(numberOfMessages)
    {
        def messages = []
        for(;numberOfMessages != 0; --numberOfMessages)
        {
            messages << DequeueMessage.createBuilder()
                                      .withId(UUID.randomUUID())
                                      .withPayload(createServiceBuffer())
                                      .withAvailableForDequeueSince(LocalDateTime.now().toEpochSecond(OffsetDateTime.now().getOffset()))
                                      .withProperties('properties')
                                      .withReplyData('replydata')
                                      .withNumberOfRedeliveries(0)
                                      .withTimestamp(LocalDateTime.now().toEpochSecond(OffsetDateTime.now().getOffset()))
                                      .build()
        }
        return messages
    }

    def createServiceBuffer()
    {
        List<byte[]> l = new ArrayList<>()
        l.add('{"hello" : "world"}'.bytes)
        def serviceData = l
        def serviceBuffer = ServiceBuffer.of(serviceType, serviceData)
        return serviceBuffer
    }

}
