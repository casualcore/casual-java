package se.kodarkatten.casual.network.protocol.messages.queue

import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel
import se.kodarkatten.casual.network.protocol.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

class CasualDequeueRequestMessageTest extends Specification
{
    @Shared
    def syncSink

    def setup()
    {
        syncSink = new LocalByteChannel()
    }

    def cleanup()
    {
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
        CasualNWMessageImpl<CasualDequeueRequestMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink)
        then:
        networkBytes != null
        msg == syncResurrectedMsg
    }
}
