package se.kodarkatten.casual.network.protocol.messages.queue

import se.kodarkatten.casual.network.protocol.io.CasualNetworkReader
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel
import se.kodarkatten.casual.network.protocol.utils.TestUtils
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
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessageImpl<CasualEnqueueReplyMessage> asyncResurrectedMsg  = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessageImpl<CasualEnqueueReplyMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink)
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
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessageImpl<CasualEnqueueReplyMessage> asyncResurrectedMsg  =  TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessageImpl<CasualEnqueueReplyMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        then:
        networkBytes != null
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg
    }

}
