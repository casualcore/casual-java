/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.test.network.frombinary


import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.CasualMessageDecoder
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import se.laz.casual.network.protocol.utils.ByteUtils
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.ResourceLoader
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.channels.WritableByteChannel

class CompleteCasualServiceCallRequestMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/bin/message.service.call.Request.1000.3100.bin'

    @Shared
    def resourceProtocolVersionGreaterOrEqualToOneThree = '/protocol/bin/message.service.call.Request.1000.3100_protocol_version_greater_or_equal_to_1_3.bin'

    @Shared
    def data

    @Shared
    def dataProtocolVersionGreaterOrEqualToOneThree

    def setupSpec()
    {
        data = ResourceLoader.getResourceAsByteArray(resource)
        dataProtocolVersionGreaterOrEqualToOneThree = ResourceLoader.getResourceAsByteArray(resourceProtocolVersionGreaterOrEqualToOneThree)
        then:
        data != null
        data.length == 182
        dataProtocolVersionGreaterOrEqualToOneThree != null
        dataProtocolVersionGreaterOrEqualToOneThree.length == 287
    }

    def "get header"()
    {
        setup:
        def headerData = Arrays.copyOfRange(dataProtocolVersionGreaterOrEqualToOneThree, 0, MessageHeaderSizes.headerNetworkSize)
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

    def "roundtrip message"()
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
        CasualNWMessageImpl<CasualServiceCallRequestMessage> msg = CasualNetworkTestReader.read(sink)
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

    def "roundtrip message protocol version equal or greater to one three"()
    {
       setup:
       List<byte[]> payload = new ArrayList<>()
       payload.add(dataProtocolVersionGreaterOrEqualToOneThree)
       def sink = new LocalByteChannel()
       payload.each{
          bytes ->
             ByteBuffer buffer = ByteBuffer.wrap(bytes)
             sink.write(buffer)
       }
       when:
       CasualServiceCallRequestMessage msg = CasualNetworkTestReader.readMessage(CasualNWMessageType.SERVICE_CALL_REQUEST, sink, 287, ProtocolVersion.VERSION_1_3)
       write(sink, msg)
       CasualServiceCallRequestMessage resurrectedMsg = CasualNetworkTestReader.readMessage(CasualNWMessageType.SERVICE_CALL_REQUEST, sink, 284, ProtocolVersion.VERSION_1_3)
       then:
       msg != null
       msg == resurrectedMsg
       Long.toUnsignedString(msg.getParentSpan()) == '9259825810226120327'
    }

   <T extends CasualNetworkTransmittable> void write(final WritableByteChannel channel, final T msg)
   {
      for(final byte[] bytes : msg.toNetworkBytes())
      {
         ByteUtils.writeFully(channel, ByteBuffer.wrap(bytes), bytes.length);
      }
   }

}
