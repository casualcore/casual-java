/*
 * Copyright (c) 2024 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.reverse.outbound

import io.netty.channel.Channel
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import spock.lang.Specification

class ReverseOutboundConnectionInformationTest extends Specification
{
   def "test ReverseOutboundConnectionInformation, with useEpoll"()
   {
      setup:
      String name = "testReverse"
      int port = 8080
      UUID domainId = UUID.randomUUID()
      String domainName = "testDomain"
      Class<? extends Channel> channelClass = EpollServerSocketChannel
      boolean useEpoll = true

      ReverseOutboundConnectionInformation connectionInfo = ReverseOutboundConnectionInformation.createBuilder()
              .withName( name )
              .withPort( port )
              .withDomainId( domainId )
              .withDomainName( domainName )
              .withUseEpoll( useEpoll )
              .withConnectionConsumer( { connection -> } )
              .build()

      expect:
      connectionInfo.getName() == name
      connectionInfo.getPort() == port
      connectionInfo.getDomainId() == domainId
      connectionInfo.getDomainName() == domainName
      connectionInfo.getChannelClass() == channelClass
      connectionInfo.isUseEpoll() == useEpoll
   }

   def "test ReverseOutboundConnectionInformation, no useEpoll"()
   {
      setup:
      String name = "testReverse"
      int port = 8080
      UUID domainId = UUID.randomUUID()
      String domainName = "testDomain"
      Class<? extends Channel> channelClass = NioServerSocketChannel
      boolean useEpoll = false

      ReverseOutboundConnectionInformation connectionInfo = ReverseOutboundConnectionInformation.createBuilder()
              .withName( name )
              .withPort( port )
              .withDomainId( domainId )
              .withDomainName( domainName )
              .withUseEpoll( useEpoll )
              .withConnectionConsumer( { connection -> } )
              .build()

      expect:
      connectionInfo.getName() == name
      connectionInfo.getPort() == port
      connectionInfo.getDomainId() == domainId
      connectionInfo.getDomainName() == domainName
      connectionInfo.getChannelClass() == channelClass
      connectionInfo.isUseEpoll() == useEpoll
   }
}
