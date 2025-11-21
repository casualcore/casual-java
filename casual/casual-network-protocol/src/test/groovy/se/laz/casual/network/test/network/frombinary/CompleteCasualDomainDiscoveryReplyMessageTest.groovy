/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.test.network.frombinary

import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.CasualMessageDecoder
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage
import se.laz.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.ResourceLoader
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

class CompleteCasualDomainDiscoveryReplyMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/b64/message.gateway.domain.discovery.reply.1000.7301.b64'

    @Shared
    def resourceProtocolVersionOneFour = '/protocol/b64/message.gateway.domain.discovery.reply.1004.7311.b64'

    @Shared
    def data

    @Shared
    def dataProtocolVersionOneFour

    def setupSpec()
    {
        data = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resource))
        dataProtocolVersionOneFour = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resourceProtocolVersionOneFour))
        then:
        data != null
        dataProtocolVersionOneFour != null
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

    def "roundtrip message - sync"()
    {
        setup:
        List<byte[]> payload = new ArrayList<>()
        payload.add(binary)
        def sink = new LocalByteChannel()
        payload.each{
            bytes ->
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer)
        }
        when:
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> msg = CasualNetworkTestReader.read(sink, protocolVersion)
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        where:
        binary                       | protocolVersion
        data                         | ProtocolVersion.VERSION_1_0
        data                         | ProtocolVersion.VERSION_1_1
        data                         | ProtocolVersion.VERSION_1_2
        data                         | ProtocolVersion.VERSION_1_3
        dataProtocolVersionOneFour   | ProtocolVersion.VERSION_1_4
    }

}
