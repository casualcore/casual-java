package se.kodarkatten.casual.network.protocol.messages.queue

import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel
import se.kodarkatten.casual.network.protocol.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

class CasualEnqueueReplyMessageTest extends Specification
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
        def requestMsg = CasualEnqueueReplyMessage.createBuilder()
                                                  .withExecution(UUID.randomUUID())
                                                  .withId(UUID.randomUUID())
                                                  .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessageImpl<CasualEnqueueReplyMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink)
        then:
        networkBytes != null
        requestMsg == syncResurrectedMsg.getMessage()
        msg == syncResurrectedMsg
    }



}
