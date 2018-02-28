package se.kodarkatten.casual.network.test.network.frombinary

import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel

import java.nio.ByteBuffer
import se.kodarkatten.casual.network.protocol.io.CasualNetworkReader
import se.kodarkatten.casual.network.protocol.io.CasualNetworkWriter
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.kodarkatten.casual.network.protocol.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.protocol.utils.ResourceLoader
import se.kodarkatten.casual.network.protocol.utils.WriteCompletionHandler
import spock.lang.Shared
import spock.lang.Specification
import java.util.concurrent.CompletableFuture

class CasualDomainConnectReplyMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/bin/message.gateway.domain.connect.Reply.7201.bin'

    @Shared
    def data

    def setupSpec()
    {
        data = ResourceLoader.getResourceAsByteArray(resource)
        then:
        data != null
        data.length == 88
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
        def sink = new LocalAsyncByteChannel()
        payload.each{
            bytes ->
                CompletableFuture<Void> future = new CompletableFuture<>()
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer, null, WriteCompletionHandler.of(future, buffer, sink))
                future.get()
        }
        when:
        CasualNWMessageImpl<CasualDomainConnectReplyMessage> msg = CasualNetworkReader.read(sink)
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainConnectReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        then:
        msg != null
        msg.message == resurrectedMsg.message
        msg == resurrectedMsg
    }

    def "roundtrip message - sync"()
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
        CasualNWMessageImpl<CasualDomainConnectReplyMessage> msg = CasualNetworkReader.read(sink)
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainConnectReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        then:
        msg != null
        msg.message == resurrectedMsg.message
        msg == resurrectedMsg
    }

}