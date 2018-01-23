package se.kodarkatten.casual.network.utils

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException

import java.nio.ByteBuffer
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class LocalByteChannelTest extends Specification
{
    @Shared
    def payload = 'very nice payload'
    def "write/read"()
    {
        setup:
        byte[] data = payload.getBytes(StandardCharsets.UTF_8)
        ByteBuffer b = ByteBuffer.wrap(data)
        LocalByteChannel sink = new LocalByteChannel()
        ByteBuffer readBuffer = ByteBuffer.allocate(data.length)
        when:
        sink.write(b)
        sink.read(readBuffer)
        then:
        readBuffer.array() == data
        payload == new String(readBuffer.array(), StandardCharsets.UTF_8)
    }

    def "write/read with ByteUtils"()
    {
        setup:
        byte[] data = payload.getBytes(StandardCharsets.UTF_8)
        ByteBuffer b = ByteBuffer.wrap(data)
        LocalByteChannel sink = new LocalByteChannel()
        when:
        ByteUtils.writeFully(sink, b, data.length)
        ByteBuffer read = ByteUtils.readFully(sink, data.length)
        then:
        read.array() == data
        payload == new String(read.array(), StandardCharsets.UTF_8)
    }

    def "write/read casual message with network writer/reader"()
    {
        setup:
        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setDomainId(UUID.randomUUID())
                .setDomainName('Home sweet home')
                .setQueueNames(Arrays.asList("queueA1"))
                .build()
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        LocalByteChannel sink = new LocalByteChannel()
        when:
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        then:
        msg == resurrectedMsg
    }

    def "readFully fail"()
    {
        setup:
        byte[] data = payload.getBytes(StandardCharsets.UTF_8)
        ByteBuffer b = ByteBuffer.wrap(data)
        LocalByteChannel sink = new LocalByteChannel()
        def badReadLength = data.length * 2
        when:
        ByteUtils.writeFully(sink, b, data.length)
        ByteUtils.readFully(sink, badReadLength)
        then:
        def e = thrown(CasualTransportException)
        e.message == "expected to read: ${badReadLength} but could only read: ${data.length} bytes, broken pipe?"
    }

}
