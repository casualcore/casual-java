/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.service

import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.xa.XID
import se.laz.casual.jca.SpanId
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.LocalByteChannel
import spock.lang.Shared
import spock.lang.Specification

class CasualServiceCallRequestMessageTest extends Specification
{
    @Shared
    def execution = UUID.randomUUID()
    @Shared
    def serviceName = 'A very fine service'
    @Shared
    def timeout = 90 * 1000
    @Shared
    def parentName = 'Jane Doe'
    @Shared
    def nullXID = XID.NULL_XID
    @Shared
    def xatmiFlags = Flag.of(AtmiFlags.TPNOBLOCK)
    @Shared
    def serviceData
    @Shared
    def serviceType = 'application/json'
    @Shared
    def serviceBuffer
    @Shared
    SpanId parentSpan = SpanId.of()

    def setupSpec()
    {
        List<byte[]> l = new ArrayList<>()
        l.add([2,3,4] as byte[])
        serviceData = l
        serviceBuffer = ServiceBuffer.of(serviceType, serviceData)
    }

    def "Message creation"() {
       setup:

       when:
       //println("protocolVersion: ${protocolVersion}")
       def msgBuilder = CasualServiceCallRequestMessage.createBuilder()
               .setExecution(execution)
               .setServiceName(serviceName)
               .setTimeout(timeout)
               .setParentName(parentName)
               .setXid(nullXID)
               .setXatmiFlags(xatmiFlags)
               .setServiceBuffer(serviceBuffer)
               .setProtocolVersion(protocolVersion)
       if (ProtocolVersion.isGreaterOrEqualToOneThree(protocolVersion))
       {
          msgBuilder.setParentSpan(parentSpan)
       }
        def msg = msgBuilder.build()
        then:
        msg.execution == execution
        msg.serviceName == serviceName
        msg.timeout == timeout
        msg.parentName == parentName
        msg.xid == nullXID
        msg.serviceBuffer == serviceBuffer
        msg.serviceBuffer.payload == serviceBuffer.payload
        if(ProtocolVersion.isGreaterOrEqualToOneThree(protocolVersion))
        {
           msg.getParentSpan() == parentSpan
        }
        where:
        protocolVersion << ProtocolVersion.values()
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - sync"()
    {
        setup:
        def requestMsgBuilder = CasualServiceCallRequestMessage.createBuilder()
                .setExecution(execution)
                .setServiceName(serviceName)
                .setTimeout(timeout)
                .setParentName(parentName)
                .setXid(nullXID)
                .setXatmiFlags(xatmiFlags)
                .setServiceBuffer(serviceBuffer)
                .setProtocolVersion(protocolVersion)

        if(ProtocolVersion.isGreaterOrEqualToOneThree(protocolVersion))
        {
           requestMsgBuilder.setParentSpan(parentSpan)
        }
        def requestMsg = requestMsgBuilder.build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)

        then:
        networkBytes != null
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        resurrectedMsg.getMessage().getServiceBuffer().getPayload().size() == 1
        requestMsg.serviceBuffer.payload == resurrectedMsg.getMessage().serviceBuffer.payload
        if(ProtocolVersion.isGreaterOrEqualToOneThree(protocolVersion))
        {
          resurrectedMsg.getMessage().getParentSpan() == parentSpan
        }
        where:
        protocolVersion << ProtocolVersion.values()
    }

}
