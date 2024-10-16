/*
 * Copyright (c) 2022 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import se.laz.casual.config.Configuration
import se.laz.casual.network.ProtocolVersion
import spock.lang.Specification

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

class NettyConnectionInformationCreatorTest extends Specification
{
   def 'default, does not use epoll'()
   {
      given:
      InetSocketAddress address = new InetSocketAddress('foo.bar', 1234)
      ProtocolVersion protocolVersion = ProtocolVersion.VERSION_1_0
      when:
      NettyConnectionInformation ci = NettyConnectionInformationCreator.create(address, protocolVersion)
      then:
      ci.getChannelClass() == NioSocketChannel.class
   }

   def 'use epoll'()
   {
      given:
      InetSocketAddress address = new InetSocketAddress('foo.bar', 1234)
      ProtocolVersion protocolVersion = ProtocolVersion.VERSION_1_0
      when:
      NettyConnectionInformation ci
      withEnvironmentVariable( Configuration.USE_EPOLL_ENV_VAR_NAME, "true" )
              .execute( {
                 ci = NettyConnectionInformationCreator.create(address, protocolVersion)
                 } )

      then:
      ci.getChannelClass() == EpollSocketChannel.class
   }
}
