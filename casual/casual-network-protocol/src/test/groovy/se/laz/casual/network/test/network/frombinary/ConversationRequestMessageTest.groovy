/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.test.network.frombinary

import se.laz.casual.network.protocol.decoding.CasualMessageDecoder
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.conversation.ConnectRequest
import se.laz.casual.network.protocol.messages.conversation.Request
import se.laz.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.ResourceLoader
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

class ConversationRequestMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/bin/message.conversation.Send.1000.3212.bin'

    @Shared
    def data

    def setupSpec()
    {
        data = ResourceLoader.getResourceAsByteArray(resource)
        println("len ${data.length}")
        then:
        assert(data != null)
        assert(data.length == 214)
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

    def 'roundtrip message'()
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
       CasualNWMessageImpl<Request> msg = CasualNetworkTestReader.read(sink)
       CasualMessageEncoder.write(sink, msg)
       CasualNWMessageImpl<Request> resurrectedMsg = CasualNetworkTestReader.read(sink)
       then:
       msg != null
       msg.getMessage() == resurrectedMsg.getMessage()
       msg == resurrectedMsg
       msg.getMessage().getServiceBuffer().getPayload() == resurrectedMsg.getMessage().getServiceBuffer().getPayload()
    }
}
