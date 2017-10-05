package se.kodarkatten.casual.network.messages.service

import se.kodarkatten.casual.api.flags.AtmiFlags
import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.utils.ByteUtils
import se.kodarkatten.casual.network.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.utils.LocalByteChannel
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
    def nullXID = XID.of()
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
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        requestMsg == resurrectedMsg.getMessage()
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
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        // force chunking when reading
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        def collectedServicePayload = collectServicePayload(resurrectedMsg.getMessage().getServiceBuffer().getPayload())
        then:
        networkBytes != null
        requestMsg == resurrectedMsg.getMessage()
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
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        // force chunking when reading
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)
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
