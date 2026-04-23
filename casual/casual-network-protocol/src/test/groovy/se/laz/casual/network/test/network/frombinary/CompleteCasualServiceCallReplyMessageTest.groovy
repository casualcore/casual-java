/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.test.network.frombinary

import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.CasualMessageDecoder
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.ResourceLoader
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

class CompleteCasualServiceCallReplyMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/b64/message.service.call.reply.1000.3101.b64'
    @Shared
    def resourceProtocolVersion_1003 = '/protocol/b64/message.service.call.reply.1003.3103.b64'

    @Shared
    def resourceProtocolVersion_1005 = '/protocol/b64/message.service.call.reply.1005.3105.b64'

    @Shared
    byte[] data
    @Shared
    byte[] dataProtocolVersion_1003
    @Shared
    byte[] dataProtocolVersion_1005

    def setupSpec()
    {
        data = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resource))
        dataProtocolVersion_1003 = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resourceProtocolVersion_1003))
        dataProtocolVersion_1005 = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resourceProtocolVersion_1005))
        then:
        data != null
        dataProtocolVersion_1005 != null
    }

    def "get header"()
    {
        setup:
        def headerData = Arrays.copyOfRange(networkData, 0, MessageHeaderSizes.headerNetworkSize)
        when:
        def header = CasualMessageDecoder.networkHeaderToCasualHeader(headerData)
        then:
        header != null

        where:
        networkData << [
                data, dataProtocolVersion_1003, dataProtocolVersion_1005
        ]
    }

    def "roundtrip header"()
    {
        setup:
        def headerData = Arrays.copyOfRange(networkData, 0, MessageHeaderSizes.headerNetworkSize)
        def header = CasualMessageDecoder.networkHeaderToCasualHeader(headerData)
        when:
        def resurrectedHeader = CasualMessageDecoder.networkHeaderToCasualHeader(header.toNetworkBytes())
        then:
        header != null
        resurrectedHeader != null
        resurrectedHeader == header
        resurrectedHeader.payloadSize == payloadSize

        where:
        networkData              | payloadSize
        data                     | 237
        dataProtocolVersion_1003 | 181
        dataProtocolVersion_1005 | 228
    }

    def "roundtrip message"()
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
        CasualNWMessageImpl<CasualServiceCallReplyMessage> msg = CasualNetworkTestReader.read(sink, protocolVersion)
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualServiceCallReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        where:
        binary                   | protocolVersion
        data                     | ProtocolVersion.VERSION_1_0
        data                     | ProtocolVersion.VERSION_1_1
        data                     | ProtocolVersion.VERSION_1_2
        dataProtocolVersion_1003 | ProtocolVersion.VERSION_1_3
        dataProtocolVersion_1003 | ProtocolVersion.VERSION_1_4
        dataProtocolVersion_1005 | ProtocolVersion.VERSION_1_5
    }

    def "Check message headers #protocolVersion"()
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
        CasualNWMessageImpl<CasualServiceCallReplyMessage> msg = CasualNetworkTestReader.read(sink, protocolVersion)

        then:
        msg.getMessage(  ).getHeaders(  ) == expectedHeaders

        where:
        binary                   | protocolVersion             | expectedHeaders
        data                     | ProtocolVersion.VERSION_1_0 | [:]
        data                     | ProtocolVersion.VERSION_1_1 | [:]
        data                     | ProtocolVersion.VERSION_1_2 | [:]
        dataProtocolVersion_1003 | ProtocolVersion.VERSION_1_3 | [:]
        dataProtocolVersion_1003 | ProtocolVersion.VERSION_1_4 | [:]
        dataProtocolVersion_1005 | ProtocolVersion.VERSION_1_5 | ["a": "foo", "b": "bar", "c": "baz"]
    }

}
