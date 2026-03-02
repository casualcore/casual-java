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
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.ResourceLoader
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

class CompleteCasualServiceCallRequestMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/b64/message.service.call.request.1000.3100.b64'

    @Shared
    def resourceProtocolVersionGreaterOrEqualToOneFour = '/protocol/b64/message.service.call.request.1003.3102.b64'

    @Shared
    def data

    @Shared
    def dataProtocolVersionGreaterOrEqualToOneThree

    def setupSpec()
    {
        data = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resource))
        dataProtocolVersionGreaterOrEqualToOneThree = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resourceProtocolVersionGreaterOrEqualToOneFour))
        then:
        data != null
        dataProtocolVersionGreaterOrEqualToOneThree != null
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

    def "roundtrip message #protocolVersion"()
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
        CasualNWMessageImpl<CasualServiceCallRequestMessage> msg = CasualNetworkTestReader.read(sink, protocolVersion)
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        where:
        binary                                        | protocolVersion
        data                                          | ProtocolVersion.VERSION_1_0
        data                                          | ProtocolVersion.VERSION_1_1
        data                                          | ProtocolVersion.VERSION_1_2
        dataProtocolVersionGreaterOrEqualToOneThree   | ProtocolVersion.VERSION_1_3
        dataProtocolVersionGreaterOrEqualToOneThree   | ProtocolVersion.VERSION_1_4
    }
}
