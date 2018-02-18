package se.kodarkatten.casual.network.protocol.messages.queue

import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.protocol.io.CasualNetworkReader
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel
import se.kodarkatten.casual.network.protocol.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

class CasualDequeueRequestMessageTest extends Specification
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
        def requestMsg = CasualDequeueRequestMessage.createBuilder()
                                                    .withExecution(UUID.randomUUID())
                                                    .withQueueName('thequeue')
                                                    .withXid(XID.NULL_XID)
                                                    .withSelectorProperties('correlationInformation')
                                                    .withSelectorUUID(UUID.randomUUID())
                                                    .withBlock(true)
                                                    .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessageImpl<CasualDequeueRequestMessage> asyncResurrectedMsg  = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessageImpl<CasualDequeueRequestMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink)
        then:
        networkBytes != null
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg
    }

    def "roundtrip - chunked"()
    {
        setup:
        def requestMsg = CasualDequeueRequestMessage.createBuilder()
                .withExecution(UUID.randomUUID())
                .withQueueName('thequeue')
                .withXid(XID.NULL_XID)
                .withSelectorProperties('correlationInformation')
                .withSelectorUUID(UUID.randomUUID())
                .withBlock(true)
                .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessageImpl<CasualDequeueRequestMessage> asyncResurrectedMsg  = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessageImpl<CasualDequeueRequestMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink)
        then:
        networkBytes != null
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg
    }

}
