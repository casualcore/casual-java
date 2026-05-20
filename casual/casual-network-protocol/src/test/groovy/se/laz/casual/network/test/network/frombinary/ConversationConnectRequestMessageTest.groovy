/*
 * Copyright (c) 2021 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.test.network.frombinary

import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.CasualMessageDecoder
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.conversation.ConnectRequest
import se.laz.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.ResourceLoader
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

class ConversationConnectRequestMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/b64/message.conversation.connect.request.1000.3210.b64'
    @Shared
    def resourceProtocolVersionOneThreeOrGreater = '/protocol/b64/message.conversation.connect.request.1003.3220.b64'

    @Shared
    byte[] data
    @Shared
    byte[] dataProtocolVersion_1003

    def setupSpec()
    {
        data = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resource))
        dataProtocolVersion_1003 = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resourceProtocolVersionOneThreeOrGreater))
        then:
        assert(data != null)
        assert(dataProtocolVersion_1003 != null)
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
                data, dataProtocolVersion_1003
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

        where:
        networkData << [
                data, dataProtocolVersion_1003
        ]
    }

    def 'roundtrip message'()
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
       CasualNWMessageImpl<ConnectRequest> msg = CasualNetworkTestReader.read(sink, protocolVersion)
       CasualMessageEncoder.write(sink, msg)
       CasualNWMessageImpl<ConnectRequest> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)
       then:
       msg != null
       msg.getMessage() == resurrectedMsg.getMessage()
       msg == resurrectedMsg
       msg.getMessage().getServiceBuffer().getPayload() == resurrectedMsg.getMessage().getServiceBuffer().getPayload()

       where:
       binary                   | protocolVersion
       data                     | ProtocolVersion.VERSION_1_0
       data                     | ProtocolVersion.VERSION_1_1
       data                     | ProtocolVersion.VERSION_1_2
       dataProtocolVersion_1003 | ProtocolVersion.VERSION_1_3
       dataProtocolVersion_1003 | ProtocolVersion.VERSION_1_4
    }
}
