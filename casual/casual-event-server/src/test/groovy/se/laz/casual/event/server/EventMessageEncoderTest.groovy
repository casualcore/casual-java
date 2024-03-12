/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import se.laz.casual.api.external.json.JsonProviderFactory
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.server.handlers.EventMessageEncoder
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.nio.charset.StandardCharsets

class EventMessageEncoderTest extends Specification
{
   def 'encode message'()
   {
      given:
      Xid transactionId = Mock(Xid)
      ChannelHandlerContext ctx = Mock(ChannelHandlerContext)
      ServiceCallEvent msg = ServiceCallEvent.createBuilder()
              .withCode(ErrorState.OK)
              .withExecution(UUID.randomUUID())
              .withOrder(Order.CONCURRENT)
              .withParent("")
              .withPending(0)
              .withPID(42)
              .withService('nice service')
              .withTransactionId(transactionId)
              .build()
      def asJsonBytes =  JsonProviderFactory.getJsonProvider().toJson(msg).getBytes(StandardCharsets.UTF_8)
      ByteBuf out = Mock(ByteBuf){
         1 * writeBytes(asJsonBytes)
      }
      def encoder = new EventMessageEncoder()
      when:
      encoder.encode(ctx, msg, out)
      then:
      noExceptionThrown()
   }
}
