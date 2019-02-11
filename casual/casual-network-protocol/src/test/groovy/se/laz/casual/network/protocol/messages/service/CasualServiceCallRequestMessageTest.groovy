/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.service

import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.xa.XID
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.ByteUtils
import se.laz.casual.network.protocol.utils.LocalByteChannel
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

/**
 * Created by aleph on 2017-03-16.
 */
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

    def setupSpec()
    {
        List<byte[]> l = new ArrayList<>()
        l.add([2,3,4] as byte[])
        serviceData = l
        serviceBuffer = ServiceBuffer.of(serviceType, serviceData)
    }

    def "Message creation"()
    {
        setup:
        when:
        def msg = CasualServiceCallRequestMessage.createBuilder()
                                          .setExecution(execution)
                                          .setServiceName(serviceName)
                                          .setTimeout(timeout)
                                          .setParentName(parentName)
                                          .setXid(nullXID)
                                          .setXatmiFlags(xatmiFlags)
                                          .setServiceBuffer(serviceBuffer)
                                          .build()
        then:
        msg.execution == execution
        msg.serviceName == serviceName
        msg.timeout == timeout
        msg.parentName == parentName
        msg.xid == nullXID
        msg.serviceBuffer == serviceBuffer
        msg.serviceBuffer.payload == serviceBuffer.payload
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - sync"()
    {
        setup:
        def requestMsg = CasualServiceCallRequestMessage.createBuilder()
                .setExecution(execution)
                .setServiceName(serviceName)
                .setTimeout(timeout)
                .setParentName(parentName)
                .setXid(nullXID)
                .setXatmiFlags(xatmiFlags)
                .setServiceBuffer(serviceBuffer)
                .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)

        then:
        networkBytes != null
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        resurrectedMsg.getMessage().getServiceBuffer().getPayload().size() == 1
        requestMsg.serviceBuffer.payload == resurrectedMsg.getMessage().serviceBuffer.payload
    }

    def collectServicePayload(List<byte[]> bytes)
    {
        ByteBuffer b = ByteBuffer.allocate((int)ByteUtils.sumNumberOfBytes(bytes))
        bytes.stream()
             .forEach({d -> b.put(d)})
        List<byte[]> l = new ArrayList<>()
        l.add(b.array())
        return l
    }

}
