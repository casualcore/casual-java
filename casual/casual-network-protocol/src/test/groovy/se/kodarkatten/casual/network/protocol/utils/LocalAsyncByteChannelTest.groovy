package se.kodarkatten.casual.network.protocol.utils

import se.kodarkatten.casual.network.protocol.io.CasualNetworkReader
import se.kodarkatten.casual.network.protocol.io.CasualNetworkWriter
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

/**
 * Created by aleph on 2017-03-16.
 */
class LocalAsyncByteChannelTest extends Specification
{
    @Shared
    def payload = 'very nice payload'
    def "write/read"()
    {
        setup:
        byte[] data = payload.getBytes(StandardCharsets.UTF_8)
        ByteBuffer b = ByteBuffer.wrap(data)
        LocalAsyncByteChannel sink = new LocalAsyncByteChannel()
        CompletableFuture<Void> writeFuture = new CompletableFuture<>()
        CompletableFuture<ByteBuffer> readFuture = new CompletableFuture<>()
        ByteBuffer readBuffer = ByteBuffer.allocate(data.length)
        when:
        sink.write(b, null, WriteCompletionHandler.of(writeFuture, b, sink))
        writeFuture.get()
        sink.read(readBuffer, null, ReadCompletionHandler.of(readBuffer, readFuture, sink))
        readFuture.get()
        then:
        readBuffer.array() == data
        payload == new String(readBuffer.array(), StandardCharsets.UTF_8)
    }

    def "write/read with ByteUtils"()
    {
        setup:
        byte[] data = payload.getBytes(StandardCharsets.UTF_8)
        ByteBuffer b = ByteBuffer.wrap(data)
        LocalAsyncByteChannel sink = new LocalAsyncByteChannel()
        when:
        ByteUtils.writeFully(sink, b).get()
        ByteBuffer read = ByteUtils.readFully(sink, data.length).get()
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
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        LocalAsyncByteChannel sink = new LocalAsyncByteChannel()
        when:
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        then:
        msg == resurrectedMsg
    }


}
