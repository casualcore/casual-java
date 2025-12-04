/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.LocalByteChannel
import spock.lang.Specification

class CasualDomainDisconnectReplyMessageTest extends Specification
{
   def "message creation"()
   {
      given:
      def execution = UUID.randomUUID()
      when:
      DomainDisconnectReplyMessage msg = DomainDisconnectReplyMessage.of(execution)
      then:
      msg.getExecution() == execution
      msg.getType() == CasualNWMessageType.DOMAIN_DISCONNECT_REPLY
   }

   def "roundtrip"()
   {
      given:
      def execution = UUID.randomUUID()
      DomainDisconnectReplyMessage msg = DomainDisconnectReplyMessage.of(execution)
      CasualNWMessageImpl completeMessage = CasualNWMessageImpl.of(UUID.randomUUID(), msg)
      def sink = new LocalByteChannel()
      when:
      def networkBytes = completeMessage.toNetworkBytes()
      CasualMessageEncoder.write(sink, completeMessage)
      CasualNWMessageImpl<DomainDisconnectReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)
      then:
      networkBytes != null
      networkBytes.size() == 2 // header + msg
      completeMessage == resurrectedMsg
      where:
      protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2, ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]

   }
}
