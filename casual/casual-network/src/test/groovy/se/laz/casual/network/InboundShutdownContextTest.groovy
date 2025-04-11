/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelId
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectRequestMessage
import spock.lang.Specification

class InboundShutdownContextTest extends Specification
{
   def 'sending messages'()
   {
      given:
      def idOne = Mock(ChannelId)
      def futureOne = Mock(ChannelFuture)
      def channelOne = Mock(Channel){
         isWritable() >> true
         id() >> idOne
         closeFuture() >> futureOne
         1 * writeAndFlush({ CasualNWMessage<DomainDisconnectRequestMessage> msg ->
            Objects.requireNonNull(msg, 'msg can not be null')
            if(! (msg.getMessage() instanceof DomainDisconnectRequestMessage)){
               throw new RuntimeException('expected type DomainDisconnectRequestMessage')
            }
         })
      }
      def idTwo = Mock(ChannelId)
      def futureTwo = Mock(ChannelFuture)
      def channelTwo = Mock(Channel){
         isWritable() >> true
         id() >> idTwo
         closeFuture() >> futureTwo
         2 * writeAndFlush({ CasualNWMessage<DomainDisconnectRequestMessage> msg ->
            Objects.requireNonNull(msg, 'msg can not be null')
            if(! (msg.getMessage() instanceof DomainDisconnectRequestMessage)){
               throw new RuntimeException('expected type DomainDisconnectRequestMessage')
            }
         })
      }
      InboundShutdownContext.add(channelOne)
      InboundShutdownContext.add(channelTwo)
      when:
      InboundShutdownContext.domainDisconnect()
      InboundShutdownContext.remove(channelOne)
      // this time we only write to channelTwo since channelOne was removed
      InboundShutdownContext.domainDisconnect()
      InboundShutdownContext.clear()
      // results in zero writes since there are no channels left
      InboundShutdownContext.domainDisconnect()
      then:
      noExceptionThrown()
   }
}
