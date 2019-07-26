/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.queue

import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.queue.QueueMessage
import se.laz.casual.api.xa.XID
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneOffset

class CasualEnqueueRequestMessageTest extends Specification
{
    @Shared
    def serviceData
    @Shared
    def serviceType = 'application/json'
    @Shared
    def serviceBuffer
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
        syncSink = new LocalByteChannel()
    }

    def cleanup()
    {
        syncSink = null
    }

    def "roundtrip"()
    {
        setup:
        EnqueueMessage enqueueMsg = EnqueueMessage.of(QueueMessage.createBuilder()
                                                       .withId(UUID.randomUUID())
                                                       .withReplyQueue("qspace:qname")
                                                       .withCorrelationInformation("correlationInformation")
                                                       .withAvailableSince(LocalDateTime.now().toInstant(ZoneOffset.UTC))
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
        CasualNWMessageImpl<CasualEnqueueRequestMessage> syncResurrectedMsg = TestUtils.roundtripMessage(msg, syncSink)
        then:
        networkBytes != null
        msg == syncResurrectedMsg

        EnqueueMessage m = msg.getMessage().getMessage()
        EnqueueMessage am = msg.getMessage().getMessage()
        EnqueueMessage sm = msg.getMessage().getMessage()
        Arrays.deepEquals( m.getPayload().getPayload().toArray(), am.getPayload().getPayload().toArray( ) )
        Arrays.deepEquals( m.getPayload().getPayload().toArray(), sm.getPayload().getPayload().toArray( ) )

    }
}
