/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server

import io.netty.channel.Channel
import io.netty.channel.group.ChannelGroup
import se.laz.casual.event.ServiceCallEvent
import spock.lang.Specification

import java.util.function.Supplier

class DefaultMessageLoopTest extends Specification
{
   def 'no continue loop'()
   {
      given:
      ChannelGroup connectedClients = Mock(ChannelGroup)
      ServiceCallEvent event = Mock(ServiceCallEvent)
      Supplier<ServiceCallEvent> eventSupplier = {event}
      when:
      DefaultMessageLoop loop = DefaultMessageLoop.of(connectedClients, eventSupplier)
      loop.handleMessages()
      then:
      0 * eventSupplier.get()
      0 * connectedClients.iterator()
   }

   def 'continue loop - once'()
   {
      given:
      ServiceCallEvent event = Mock(ServiceCallEvent)
      Channel channel = Mock(Channel){
         1 * writeAndFlush(event)
      }
      Set<Channel> channels = new HashSet<>()
      channels.add(channel)
      ChannelGroup connectedClients = Mock(ChannelGroup) {
         1 * iterator() >> channels.iterator()
      }
      Supplier<ServiceCallEvent> eventSupplier = Mock(Supplier){
         1 * get() >> event
      }
      Supplier<Boolean> continueLoop = Mock(Supplier){
         2 * get() >>> [true, false]
      }
      when:
      DefaultMessageLoop loop = DefaultMessageLoop.of(connectedClients, eventSupplier)
      loop.accept(continueLoop)
      loop.handleMessages()
      then:
      noExceptionThrown()
   }

}
