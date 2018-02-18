package se.kodarkatten.casual.network.test.network.frombinary

import se.kodarkatten.casual.network.protocol.io.CasualNetworkReader
import se.kodarkatten.casual.network.protocol.io.CasualNetworkWriter
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitReplyMessage
import se.kodarkatten.casual.network.protocol.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel
import se.kodarkatten.casual.network.protocol.utils.ResourceLoader
import se.kodarkatten.casual.network.protocol.utils.WriteCompletionHandler
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

/**
 * Created by aleph on 2017-04-03.
 */
class CasualTransactionResourceCommitReplyMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/bin/message.transaction.resource.commit.Request.5203.bin'

    @Shared
    def data

    def setupSpec()
    {
        data = ResourceLoader.getResourceAsByteArray(resource)
        then:
        data != null
        data.length == 116
    }

    def "get header"()
    {
        setup:
        def headerData = Arrays.copyOfRange(data, 0, MessageHeaderSizes.headerNetworkSize)
        when:
        def header = CasualNetworkReader.networkHeaderToCasualHeader(headerData)
        then:
        header != null
    }

    def "roundtrip header"()
    {
        setup:
        def headerData = Arrays.copyOfRange(data, 0, MessageHeaderSizes.headerNetworkSize)
        def header = CasualNetworkReader.networkHeaderToCasualHeader(headerData)
        when:
        def resurrectedHeader = CasualNetworkReader.networkHeaderToCasualHeader(header.toNetworkBytes())
        then:
        header != null
        resurrectedHeader != null
        resurrectedHeader == header
    }

    def "roundtrip message - no chunking"()
    {
        setup:
        List<byte[]> payload = new ArrayList<>()
        payload.add(data)
        def sink = new LocalAsyncByteChannel()
        payload.each{
            bytes ->
                CompletableFuture<Void> future = new CompletableFuture<>()
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer, null, WriteCompletionHandler.of(future, buffer, sink))
                future.get()
        }
        when:
        CasualNWMessageImpl<CasualTransactionResourceCommitReplyMessage> msg = CasualNetworkReader.read(sink)
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualTransactionResourceCommitReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

    def "roundtrip message - chunking"()
    {
        setup:
        List<byte[]> payload = new ArrayList<>()
        payload.add(data)
        def sink = new LocalAsyncByteChannel()
        payload.each{
            bytes ->
                CompletableFuture<Void> future = new CompletableFuture<>()
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer, null, WriteCompletionHandler.of(future, buffer, sink))
                future.get()
        }
        when:
        CasualNWMessageImpl<CasualTransactionResourceCommitReplyMessage> msg = CasualNetworkReader.read(sink)
        msg.getMessage().maxMessageSize = 1
        CasualNetworkWriter.write(sink, msg)
        // force chunking when reading
        CasualNetworkReader.maxSingleBufferByteSize = 1
        CasualNWMessageImpl<CasualTransactionResourceCommitReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.maxSingleBufferByteSize = Integer.MAX_VALUE
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

    def "roundtrip message sync - no chunking"()
    {
        setup:
        List<byte[]> payload = new ArrayList<>()
        payload.add(data)
        def sink = new LocalByteChannel()
        payload.each{
            bytes ->
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer)
        }
        when:
        CasualNWMessageImpl<CasualTransactionResourceCommitReplyMessage> msg = CasualNetworkReader.read(sink)
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualTransactionResourceCommitReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

    def "roundtrip message sync - chunking"()
    {
        setup:
        List<byte[]> payload = new ArrayList<>()
        payload.add(data)
        def sink = new LocalByteChannel()
        payload.each{
            bytes ->
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer)
        }
        when:
        CasualNWMessageImpl<CasualTransactionResourceCommitReplyMessage> msg = CasualNetworkReader.read(sink)
        msg.getMessage().maxMessageSize = 1
        CasualNetworkWriter.write(sink, msg)
        // force chunking when reading
        CasualNetworkReader.maxSingleBufferByteSize = 1
        CasualNWMessageImpl<CasualTransactionResourceCommitReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.maxSingleBufferByteSize = Integer.MAX_VALUE
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

}
