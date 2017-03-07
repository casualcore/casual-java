package se.kodarkatten.casual.network

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.messages.parseinfo.MessageHeaderSizes
import se.kodarkatten.casual.network.utils.ResourceLoader
import spock.lang.Specification

/**
 * Created by aleph on 2017-03-03.
 */
class CompleteCasualDomainDiscoveryRequestMessageTest extends Specification
{
    def "Make sure resource is available"()
    {
        setup:
        def resource = '/protocol/bin/message.interdomain.domain.discovery.receive.Request.bin'
        when:
        def data = ResourceLoader.getResourceAsByteArray(resource)
        then:
        data != null
        data.length == 186
    }

    def "get header"()
    {
        setup:
        def resource = '/protocol/bin/message.interdomain.domain.discovery.receive.Request.bin'
        def data = ResourceLoader.getResourceAsByteArray(resource)
        def headerData = Arrays.copyOfRange(data, 0, MessageHeaderSizes.headerNetworkSize)
        when:
        def header = CasualNetworkReader.networkHeaderToCasualHeader(headerData)
        then:
        header != null
    }

    def "roundtrip header"()
    {
        setup:
        def resource = '/protocol/bin/message.interdomain.domain.discovery.receive.Request.bin'
        def data = ResourceLoader.getResourceAsByteArray(resource)
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
        def resource = '/protocol/bin/message.interdomain.domain.discovery.receive.Request.bin'
        def data = ResourceLoader.getResourceAsByteArray(resource)
        def headerData = Arrays.copyOfRange(data, 0, MessageHeaderSizes.headerNetworkSize)
        def header = CasualNetworkReader.networkHeaderToCasualHeader(headerData)
        def payload = new ArrayList<byte[]>()
        payload.add(Arrays.copyOfRange(data, MessageHeaderSizes.headerNetworkSize, (int)header.getPayloadSize() + MessageHeaderSizes.headerNetworkSize))
        when:
        def msg = CasualNetworkReader.networkDomainDiscoveryRequestToCasualDomainDiscoveryRequestMessage(payload)
        then:
        msg != null
    }

    def "payload to message roundtripping ( without header)"()
    {
        setup:
        def resource = '/protocol/bin/message.interdomain.domain.discovery.receive.Request.bin'
        def data = ResourceLoader.getResourceAsByteArray(resource)
        def headerData = Arrays.copyOfRange(data, 0, MessageHeaderSizes.headerNetworkSize)
        def header = CasualNetworkReader.networkHeaderToCasualHeader(headerData)
        def payload = new ArrayList<byte[]>()
        payload.add(Arrays.copyOfRange(data, MessageHeaderSizes.headerNetworkSize, (int)header.getPayloadSize() + MessageHeaderSizes.headerNetworkSize))
        def msg = CasualNetworkReader.networkDomainDiscoveryRequestToCasualDomainDiscoveryRequestMessage(payload)
        when:
        def resurrected = CasualNetworkReader.networkDomainDiscoveryRequestToCasualDomainDiscoveryRequestMessage(msg.toNetworkBytes())
        then:
        resurrected != null
        resurrected == msg
    }


}
