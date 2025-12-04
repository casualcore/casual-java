/*
 * Copyright (c) 2022-2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.test.network.frombinary

import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.CasualMessageDecoder
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectRequestMessage
import se.laz.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.ResourceLoader
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

class DomainDisconnectRequestMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/b64/message.gateway.domain.disconnect.request.1001.7202.b64'

    @Shared
    def data

    def setupSpec()
    {
        data = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resource))
        then:
        data != null
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
        payload.add(data)
        def sink = new LocalByteChannel()
        payload.each{
            bytes ->
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer)
        }
        when:
        CasualNWMessageImpl<DomainDisconnectRequestMessage> msg = CasualNetworkTestReader.read(sink, protocolVersion)
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<DomainDisconnectRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)
        then:
        msg != null
        msg.message == resurrectedMsg.message
        msg == resurrectedMsg
        where:
        protocolVersion << [ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2, ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

}
