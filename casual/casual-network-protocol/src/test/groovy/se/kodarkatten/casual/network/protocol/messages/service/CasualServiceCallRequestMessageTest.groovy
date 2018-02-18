package se.kodarkatten.casual.network.protocol.messages.service

import se.kodarkatten.casual.api.flags.AtmiFlags
import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.protocol.io.CasualNetworkReader
import se.kodarkatten.casual.network.protocol.io.CasualNetworkWriter
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.utils.ByteUtils
import se.kodarkatten.casual.network.protocol.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel
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

    def "Roundtrip with message payload less than Integer.MAX_VALUE"()
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
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        msg == resurrectedMsg
        resurrectedMsg.getMessage().getServiceBuffer().getPayload().size() == 1
        requestMsg.serviceBuffer.payload == resurrectedMsg.getMessage().serviceBuffer.payload
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - forcing chunking"()
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
        // force write chunking
        requestMsg.setMaxMessageSize(1)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        // force chunking when reading
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessageImpl<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        def collectedServicePayload = collectServicePayload(resurrectedMsg.getMessage().getServiceBuffer().getPayload())
        then:
        networkBytes != null
        msg == resurrectedMsg
        resurrectedMsg.getMessage().getServiceBuffer().getPayload().size() == serviceBuffer.payload.get(0).length
        requestMsg.getServiceBuffer().getPayload() == collectedServicePayload
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
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        resurrectedMsg.getMessage().getServiceBuffer().getPayload().size() == 1
        requestMsg.serviceBuffer.payload == resurrectedMsg.getMessage().serviceBuffer.payload
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - forcing chunking - sync"()
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
        // force write chunking
        requestMsg.setMaxMessageSize(1)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        // force chunking when reading
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessageImpl<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        def collectedServicePayload = collectServicePayload(resurrectedMsg.getMessage().getServiceBuffer().getPayload())
        then:
        networkBytes != null
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        resurrectedMsg.getMessage().getServiceBuffer().getPayload().size() == serviceBuffer.payload.get(0).length
        requestMsg.getServiceBuffer().getPayload() == collectedServicePayload
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
