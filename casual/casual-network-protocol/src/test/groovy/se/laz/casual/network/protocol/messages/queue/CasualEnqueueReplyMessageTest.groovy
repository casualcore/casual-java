/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.queue

import se.laz.casual.api.queue.QueueErrorCode
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3

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
        def requestMsgBuilder = CasualEnqueueReplyMessage.createBuilder()
                                                  .withExecution(UUID.randomUUID())
                                                  .withId(UUID.randomUUID())
                                                  .withProtocolVersion(protocolVersion)
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) )
        {
           requestMsgBuilder.withCode(QueueErrorCode.OK)
        }
        def requestMsg = requestMsgBuilder.build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessageImpl<CasualEnqueueReplyMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink, protocolVersion)
        then:
        networkBytes != null
        requestMsg == syncResurrectedMsg.getMessage()
        msg == syncResurrectedMsg
        where:
        protocolVersion << ProtocolVersion.values()
    }



}
