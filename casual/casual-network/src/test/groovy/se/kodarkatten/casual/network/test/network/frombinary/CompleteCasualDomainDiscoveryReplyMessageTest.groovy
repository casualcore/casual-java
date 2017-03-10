package se.kodarkatten.casual.network.test.network.frombinary

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.parseinfo.MessageHeaderSizes
import se.kodarkatten.casual.network.utils.ResourceLoader
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

/**
 * Created by aleph on 2017-03-09.
 */
class CompleteCasualDomainDiscoveryReplyMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/bin/message.interdomain.domain.discovery.receive.Reply.bin'

    @Shared
    def data

    def setupSpec()
    {
        data = ResourceLoader.getResourceAsByteArray(resource)
        then:
        data != null
        data.length == 167
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

    def "payload to message"()
    {
        setup:
        def headerData = Arrays.copyOfRange(data, 0, MessageHeaderSizes.headerNetworkSize)
        def header = CasualNetworkReader.networkHeaderToCasualHeader(headerData)
        def payload = new ArrayList<byte[]>()
        payload.add(Arrays.copyOfRange(data, MessageHeaderSizes.headerNetworkSize, (int)header.getPayloadSize() + MessageHeaderSizes.headerNetworkSize))
        when:
        def msg = CasualNetworkReader.networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(payload)
        then:
        msg != null
    }

    def "payload to message roundtripping ( without header)"()
    {
        setup:
        def headerData = Arrays.copyOfRange(data, 0, MessageHeaderSizes.headerNetworkSize)
        def header = CasualNetworkReader.networkHeaderToCasualHeader(headerData)
        def payload = new ArrayList<byte[]>()
        payload.add(Arrays.copyOfRange(data, MessageHeaderSizes.headerNetworkSize, (int)header.getPayloadSize() + MessageHeaderSizes.headerNetworkSize))
        def msg = CasualNetworkReader.networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(payload)
        when:
        def resurrected = CasualNetworkReader.networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(msg.toNetworkBytes())
        then:
        resurrected != null
        resurrected == msg
    }

    def "payload to message roundtripping with header"()
    {
        setup:
        def headerData = Arrays.copyOfRange(data, 0, MessageHeaderSizes.headerNetworkSize)
        def header = CasualNetworkReader.networkHeaderToCasualHeader(headerData)
        def payload = new ArrayList<byte[]>()
        payload.add(Arrays.copyOfRange(data, MessageHeaderSizes.headerNetworkSize, (int)header.getPayloadSize() + MessageHeaderSizes.headerNetworkSize))
        def msg = CasualNetworkReader.networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(payload)
        CasualNWMessage nwMessage = CasualNWMessage.of(header.correlationId, msg)
        def msgBytes = nwMessage.toNetworkBytes()
        when:
        def resurrectedHeader = CasualNetworkReader.networkHeaderToCasualHeader(msgBytes.get(0))
        def casualDomainDiscoveryRequestMsgBytes = []
        casualDomainDiscoveryRequestMsgBytes << ByteBuffer.wrap(msgBytes.get(1), 0, (int)resurrectedHeader.payloadSize).array()
        def resurrectedMsg = CasualNetworkReader.networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(casualDomainDiscoveryRequestMsgBytes)
        then:
        resurrectedHeader != null
        header == resurrectedHeader
        resurrectedMsg != null
        resurrectedMsg == msg
    }


}
