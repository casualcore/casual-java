package se.kodarkatten.casual.network.messages.queue

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.utils.LocalByteChannel
import se.kodarkatten.casual.network.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

class CasualEnqueueReplyMessageTest extends Specification
{
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
        def requestMsg = CasualEnqueueReplyMessage.createBuilder()
                                                  .withExecution(UUID.randomUUID())
                                                  .withId(UUID.randomUUID())
                                                  .build()
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessage<CasualEnqueueReplyMessage> asyncResurrectedMsg  = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessage<CasualEnqueueReplyMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink)
        then:
        networkBytes != null
        requestMsg == asyncResurrectedMsg.getMessage()
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg
    }

    def "roundtrip - chunked"()
    {
        setup:
        def requestMsg = CasualEnqueueReplyMessage.createBuilder()
                .withExecution(UUID.randomUUID())
                .withId(UUID.randomUUID())
                .build()
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualEnqueueReplyMessage> asyncResurrectedMsg  =  TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessage<CasualEnqueueReplyMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        then:
        networkBytes != null
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg
    }

}
