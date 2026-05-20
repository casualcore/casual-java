/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.service

import se.laz.casual.api.buffer.CasualHeaders
import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.api.xa.XID
import se.laz.casual.jca.SpanId
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.LocalByteChannel
import spock.lang.Shared
import spock.lang.Specification

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3
import static se.laz.casual.network.ProtocolVersion.VERSION_1_5

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
    def serviceBufferWithHeaders
    @Shared
    SpanId parentSpan = SpanId.of()
    @Shared
    List<String> rawHeaders = ["a:foo", "b:bar","c:baz"]
    @Shared
    CasualHeaders headers

    def setupSpec()
    {
        List<byte[]> l = new ArrayList<>()
        l.add([2,3,4] as byte[])
        serviceData = l
        serviceBuffer = ServiceBuffer.of(serviceType, serviceData)

        headers = CasualHeaders.newBuilder().addAll( rawHeaders ).build(  )
        serviceBufferWithHeaders = ServiceBuffer.of( serviceType, serviceData, headers )
    }

    def "Message creation"()
    {
        setup:
        ServiceBuffer buffer = protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5 )
                ? serviceBufferWithHeaders : serviceBuffer

        when:
        //println("protocolVersion: ${protocolVersion}")
        def msgBuilder = CasualServiceCallRequestMessage.createBuilder()
                .setExecution( execution )
                .setServiceName( serviceName )
                .setTimeout( timeout )
                .setParentName( parentName )
                .setXid( nullXID )
                .setXatmiFlags( xatmiFlags )
                .setServiceBuffer( buffer )
                .setProtocolVersion( protocolVersion )
        if ( protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) )
        {
            msgBuilder.setParentSpan( parentSpan )
        }

        def msg = msgBuilder.build()
        then:
        msg.execution == execution
        msg.serviceName == serviceName
        msg.timeout == timeout
        msg.parentName == parentName
        msg.xid == nullXID

        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ))
        {
           msg.getParentSpan() == parentSpan
        }

        msg.getServiceBuffer(  ) == buffer

        msg.getType(  ) == type

        where:
        protocolVersion             | type
        ProtocolVersion.VERSION_1_0 | CasualNWMessageType.SERVICE_CALL_REQUEST
        ProtocolVersion.VERSION_1_1 | CasualNWMessageType.SERVICE_CALL_REQUEST
        ProtocolVersion.VERSION_1_2 | CasualNWMessageType.SERVICE_CALL_REQUEST
        ProtocolVersion.VERSION_1_3 | CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_3
        ProtocolVersion.VERSION_1_4 | CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_3
        ProtocolVersion.VERSION_1_5 | CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_5
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - sync"()
    {
        setup:
        ServiceBuffer buffer = protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5 )
                ? serviceBufferWithHeaders : serviceBuffer

        def requestMsgBuilder = CasualServiceCallRequestMessage.createBuilder()
                .setExecution(execution)
                .setServiceName(serviceName)
                .setTimeout(timeout)
                .setParentName(parentName)
                .setXid(nullXID)
                .setXatmiFlags(xatmiFlags)
                .setServiceBuffer(buffer)
                .setProtocolVersion(protocolVersion)

        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ))
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
        requestMsg.getServiceBuffer().getPayload(  ) == resurrectedMsg.getMessage().getServiceBuffer(  ).getPayload(  )

        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ))
        {
          resurrectedMsg.getMessage().getParentSpan() == parentSpan
        }
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5 ))
        {
            resurrectedMsg.getMessage(  ).getServiceBuffer(  ).getHeaders(  ) == headers
        }

        msg.getType(  ) == type

        where:
        protocolVersion             | type
//        ProtocolVersion.VERSION_1_0 | CasualNWMessageType.SERVICE_CALL_REQUEST
//        ProtocolVersion.VERSION_1_1 | CasualNWMessageType.SERVICE_CALL_REQUEST
//        ProtocolVersion.VERSION_1_2 | CasualNWMessageType.SERVICE_CALL_REQUEST
//        ProtocolVersion.VERSION_1_3 | CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_3
//        ProtocolVersion.VERSION_1_4 | CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_3
        ProtocolVersion.VERSION_1_5 | CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_5
    }

}
