/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import se.laz.casual.config.ConfigurationOptions
import se.laz.casual.config.ConfigurationService
import spock.lang.Specification

class NettyConnectionInformationCreatorTest extends Specification
{
   def cleanup()
   {
      ConfigurationService.reload(  )
   }

   def 'default, does not use epoll'()
   {
      given:
      InetSocketAddress address = new InetSocketAddress('foo.bar', 1234)
      when:
      NettyConnectionInformation ci = NettyConnectionInformationCreator.create(address)
      then:
      ci.getChannelClass() == NioSocketChannel.class
   }

   def 'use epoll'()
   {
      given:
      InetSocketAddress address = new InetSocketAddress('foo.bar', 1234)
      ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL, true )

      when:
      NettyConnectionInformation ci = NettyConnectionInformationCreator.create(address)

      then:
      ci.getChannelClass() == EpollSocketChannel.class
   }
}
