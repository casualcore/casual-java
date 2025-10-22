/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.test.network.frombinary


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
    def resourceProtocolVersionGreaterOrEqualToOneFour = '/protocol/bin/message.service.call.Request.3102.bin'

    @Shared
    def data

    @Shared
    def dataProtocolVersionGreaterOrEqualToOneThree

    def setupSpec()
    {
        data = ResourceLoader.getResourceAsByteArray(resource)
        dataProtocolVersionGreaterOrEqualToOneThree = ResourceLoader.getResourceAsByteArray(resourceProtocolVersionGreaterOrEqualToOneFour)
        then:
        data != null
        data.length == 182
        dataProtocolVersionGreaterOrEqualToOneThree != null
        dataProtocolVersionGreaterOrEqualToOneThree.length == 205
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
        CasualNWMessageImpl<CasualServiceCallRequestMessage> msg = CasualNetworkTestReader.read(sink, ProtocolVersion.VERSION_1_0)
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, ProtocolVersion.VERSION_1_0)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

   def 'roundtrip message protocol version 1.4'()
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
      CasualNWMessageImpl<CasualServiceCallRequestMessage> msg = CasualNetworkTestReader.read(sink, ProtocolVersion.VERSION_1_4)
      CasualMessageEncoder.write(sink, msg)
      CasualNWMessageImpl<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, ProtocolVersion.VERSION_1_4)
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
       CasualNWMessageImpl<CasualServiceCallRequestMessage> msg = CasualNetworkTestReader.read(sink, ProtocolVersion.VERSION_1_3)
       CasualMessageEncoder.write(sink, msg)
       CasualNWMessageImpl<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, ProtocolVersion.VERSION_1_3)
       then:
       msg != null
       msg.getMessage() == resurrectedMsg.getMessage()
       msg == resurrectedMsg
       msg.getMessage().getParentSpan() != null
    }

   <T extends CasualNetworkTransmittable> void write(final WritableByteChannel channel, final T msg)
   {
      for(final byte[] bytes : msg.toNetworkBytes())
      {
         ByteUtils.writeFully(channel, ByteBuffer.wrap(bytes), bytes.length);
      }
   }

}
