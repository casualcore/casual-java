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

class CasualDomainDisconnectRequestMessageTest extends Specification
{
   def "message creation"()
   {
      given:
      def execution = UUID.randomUUID()
      when:
      DomainDisconnectRequestMessage msg = DomainDisconnectRequestMessage.of(execution)
      then:
      msg.getExecution() == execution
      msg.getType() == CasualNWMessageType.DOMAIN_DISCONNECT_REQUEST
   }

   def "roundtrip"()
   {
      given:
      def execution = UUID.randomUUID()
      DomainDisconnectRequestMessage msg = DomainDisconnectRequestMessage.of(execution)
      CasualNWMessageImpl completeMessage = CasualNWMessageImpl.of(UUID.randomUUID(), msg)
      def sink = new LocalByteChannel()
      when:
      def networkBytes = completeMessage.toNetworkBytes()
      CasualMessageEncoder.write(sink, completeMessage)
      CasualNWMessageImpl<DomainDisconnectRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)
      then:
      networkBytes != null
      networkBytes.size() == 2 // header + msg
      completeMessage == resurrectedMsg
      where:
      protocolVersion << ProtocolVersion.values()

   }
}
