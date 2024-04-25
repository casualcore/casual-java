/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server


import io.netty.channel.group.ChannelGroup
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.ServiceCallEventStore
import spock.lang.Specification

import java.util.function.BooleanSupplier

class DefaultMessageLoopTest extends Specification
{
   def 'no continue loop'()
   {
      given:
      ChannelGroup connectedClients = Mock(ChannelGroup)
      ServiceCallEventStore store = Mock(ServiceCallEventStore) {
         0 * take()
      }
      when:
      DefaultMessageLoop loop = DefaultMessageLoop.of(connectedClients, store)
      loop.handleMessages()
      then:
      0 * connectedClients.iterator()
   }

   def 'continue loop - once'()
   {
      given:
      ServiceCallEvent event = Mock(ServiceCallEvent)
      ChannelGroup connectedClients = Mock(ChannelGroup) {
         1 * writeAndFlush(event)
      }
      ServiceCallEventStore store = Mock(ServiceCallEventStore) {
         1 * take() >> event
      }
      BooleanSupplier continueLoop = Mock(BooleanSupplier){
         2 * getAsBoolean() >>> [true, false]
      }
      when:
      DefaultMessageLoop loop = DefaultMessageLoop.of(connectedClients, store)
      loop.accept(continueLoop)
      loop.handleMessages()
      then:
      noExceptionThrown()
   }

}
