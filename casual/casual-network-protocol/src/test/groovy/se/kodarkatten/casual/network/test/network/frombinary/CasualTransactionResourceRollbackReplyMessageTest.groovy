package se.kodarkatten.casual.network.test.network.frombinary

import se.kodarkatten.casual.network.protocol.decoding.CasualMessageDecoder
import se.kodarkatten.casual.network.protocol.decoding.CasualNetworkTestReader
import se.kodarkatten.casual.network.protocol.encoding.CasualMessageEncoder
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackReplyMessage
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel
import se.kodarkatten.casual.network.protocol.utils.ResourceLoader
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

/**
 * Created by aleph on 2017-04-03.
 */
class CasualTransactionResourceRollbackReplyMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/bin/message.transaction.resource.rollback.Reply.5206.bin'

    @Shared
    def data

    def setupSpec()
    {
        data = ResourceLoader.getResourceAsByteArray(resource)
        then:
        data != null
        data.length == 112
    }

    def "get header"()
    {
        setup:
        def headerData = Arrays.copyOfRange(data, 0, MessageHeaderSizes.headerNetworkSize)
        when:
        def header = CasualMessageDecoder.networkHeaderToCasualHeader(headerData)
        then:
        header != null
    }

    def "roundtrip header"()
    {
        setup:
        def headerData = Arrays.copyOfRange(data, 0, MessageHeaderSizes.headerNetworkSize)
        def header = CasualMessageDecoder.networkHeaderToCasualHeader(headerData)
        when:
        def resurrectedHeader = CasualMessageDecoder.networkHeaderToCasualHeader(header.toNetworkBytes())
        then:
        header != null
        resurrectedHeader != null
        resurrectedHeader == header
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
        CasualNWMessageImpl<CasualTransactionResourceRollbackReplyMessage> msg = CasualNetworkTestReader.read(sink)
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualTransactionResourceRollbackReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }
}
