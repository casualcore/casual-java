package se.laz.casual.network.test.network.frombinary

import se.laz.casual.network.protocol.decoding.CasualMessageDecoder
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.laz.casual.network.protocol.messages.queue.CasualDequeueRequestMessage
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.ResourceLoader
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

class DequeueRequestMessageTest extends Specification
{
   @Shared
   def resource = '/protocol/b64/message.queue.dequeue.request.1000.6200.b64'

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
      CasualNWMessageImpl<CasualDequeueRequestMessage> msg = CasualNetworkTestReader.read(sink)
      CasualMessageEncoder.write(sink, msg)
      CasualNWMessageImpl<CasualDequeueRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)
      then:
      msg != null
      msg.getMessage() == resurrectedMsg.getMessage()
      msg == resurrectedMsg
   }
}
