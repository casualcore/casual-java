package se.kodarkatten.casual.network.test.network.frombinary

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.parseinfo.MessageHeaderSizes
import se.kodarkatten.casual.network.messages.request.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.utils.LocalByteChannel
import se.kodarkatten.casual.network.utils.ResourceLoader
import se.kodarkatten.casual.network.utils.WriteCompletionHandler
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

/**
 * Created by aleph on 2017-03-17.
 */
class CompleteCasualServiceCallRequestMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/bin/message.interdomain.service.call.receive.Request.bin'

    @Shared
    def data

    def setupSpec()
    {
        data = ResourceLoader.getResourceAsByteArray(resource)
        then:
        data != null
        data.length == 190
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

    def "roundtrip message"()
    {
        setup:
        List<byte[]> payload = new ArrayList<>()
        payload.add(data)
        def sink = new LocalByteChannel()
        payload.each{
            bytes ->
                CompletableFuture<Void> future = new CompletableFuture<>()
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer, null, WriteCompletionHandler.of(future, buffer, sink))
                future.get()
        }
        when:
        CasualNWMessage<CasualServiceCallRequestMessage> msg = CasualNetworkReader.read(sink)
        CasualNetworkWriter.write(sink, msg)
        // force chunking when reading
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

}
