/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server

import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class EventServerTest extends Specification
{
   def 'connect'()
   {
      given:
      ServerInitialization serverInitialization = DefaultServerInitialization.of()
      EventServerConnectionInformation connectionInformation = EventServerConnectionInformation.createBuilder()
              .withServerInitialization (serverInitialization)
              .withPort(0)
              .build()
      EventServer server = EventServer.of(connectionInformation)
      InetSocketAddress address = (InetSocketAddress) server.channel.localAddress()
      when:
      connect(address)
      then:
      server.isActive()
      when:
      server.close()
      then:
      !server.isActive()
   }

   def connect(InetSocketAddress address)
   {
      SocketChannel socketChannel = SocketChannel.open(address)
      byte[] payload = '{"message":"HELLO"}' as byte[]
      ByteBuffer buffer = ByteBuffer.wrap(payload)
      socketChannel.write(buffer)
   }
}
