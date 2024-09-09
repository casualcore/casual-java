/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound.reverse

import io.netty.channel.Channel
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.outbound.Correlator
import spock.lang.Specification

import javax.resource.spi.XATerminator
import javax.resource.spi.endpoint.MessageEndpointFactory
import javax.resource.spi.work.WorkManager

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

class ReverseInboundConnectionInformationTest extends Specification
{
   def "test ReverseInboundConnectionInformation, with useEpoll"()
   {
      setup:
      InetSocketAddress address = InetSocketAddress.createUnresolved("localhost", 8080)
      ProtocolVersion protocolVersion = ProtocolVersion.VERSION_1_0
      Correlator correlator = Mock(Correlator)
      MessageEndpointFactory factory = Mock(MessageEndpointFactory)
      XATerminator xaTerminator = Mock(XATerminator)
      WorkManager workManager = Mock(WorkManager)
      UUID domainId = UUID.randomUUID()
      String domainName = "testDomain"
      Class<? extends Channel> channelClass = EpollSocketChannel
      boolean useEpoll = true
      long maxBackoffMillis = 12345
      boolean isLogHandlerEnabled = true

      ReverseInboundConnectionInformation connectionInfo
      withEnvironmentVariable(ReverseInboundConnectionInformation.USE_LOG_HANDLER_ENV_NAME, isLogHandlerEnabled.toString()).execute( {
         connectionInfo = ReverseInboundConnectionInformation.createBuilder()
                 .withAddress(address)
                 .withProtocolVersion(protocolVersion)
                 .withCorrelator(correlator)
                 .withFactory(factory)
                 .withXaTerminator(xaTerminator)
                 .withWorkManager(workManager)
                 .withDomainId(domainId)
                 .withDomainName(domainName)
                 .withUseEpoll(useEpoll)
                 .withMaxBackoffMillils(maxBackoffMillis)
                 .build()
      } )

      expect:
      connectionInfo.getAddress() == address
      connectionInfo.getProtocolVersion() == protocolVersion
      connectionInfo.getCorrelator() == correlator
      connectionInfo.getFactory() == factory
      connectionInfo.getXaTerminator() == xaTerminator
      connectionInfo.getWorkManager() == workManager
      connectionInfo.getDomainId() == domainId
      connectionInfo.getDomainName() == domainName
      connectionInfo.isUseEpoll() == useEpoll
      connectionInfo.getMaxBackoffMillis() == maxBackoffMillis
      connectionInfo.getChannelClass() == channelClass
      connectionInfo.isLogHandlerEnabled() == isLogHandlerEnabled
   }

   def "test ReverseInboundConnectionInformation, no useEpoll"()
   {
      setup:
      InetSocketAddress address = InetSocketAddress.createUnresolved("localhost", 8080)
      ProtocolVersion protocolVersion = ProtocolVersion.VERSION_1_0
      Correlator correlator = Mock(Correlator)
      MessageEndpointFactory factory = Mock(MessageEndpointFactory)
      XATerminator xaTerminator = Mock(XATerminator)
      WorkManager workManager = Mock(WorkManager)
      UUID domainId = UUID.randomUUID()
      String domainName = "testDomain"
      Class<? extends Channel> channelClass = NioSocketChannel
      boolean useEpoll = false
      long maxBackoffMillis = 12345
      boolean isLogHandlerEnabled = false

      ReverseInboundConnectionInformation connectionInfo
      withEnvironmentVariable(ReverseInboundConnectionInformation.USE_LOG_HANDLER_ENV_NAME, isLogHandlerEnabled.toString()).execute( {
         connectionInfo = ReverseInboundConnectionInformation.createBuilder()
                 .withAddress(address)
                 .withProtocolVersion(protocolVersion)
                 .withCorrelator(correlator)
                 .withFactory(factory)
                 .withXaTerminator(xaTerminator)
                 .withWorkManager(workManager)
                 .withDomainId(domainId)
                 .withDomainName(domainName)
                 .withUseEpoll(useEpoll)
                 .withMaxBackoffMillils(maxBackoffMillis)
                 .build()
      } )

      expect:
      connectionInfo.getAddress() == address
      connectionInfo.getProtocolVersion() == protocolVersion
      connectionInfo.getCorrelator() == correlator
      connectionInfo.getFactory() == factory
      connectionInfo.getXaTerminator() == xaTerminator
      connectionInfo.getWorkManager() == workManager
      connectionInfo.getDomainId() == domainId
      connectionInfo.getDomainName() == domainName
      connectionInfo.isUseEpoll() == useEpoll
      connectionInfo.getMaxBackoffMillis() == maxBackoffMillis
      connectionInfo.getChannelClass() == channelClass
      connectionInfo.isLogHandlerEnabled() == isLogHandlerEnabled
   }
}
