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
import se.laz.casual.network.protocol.messages.domain.DomainDiscoveryTopologyUpdateMessage
import spock.lang.Specification

class InboundTopologyUpdateContextTest extends Specification
{
   def 'sending messages'()
   {
      given:
      def execution = UUID.randomUUID()
      def idOne = Mock(ChannelId)
      def futureOne = Mock(ChannelFuture)
      def channelOne = Mock(Channel){
         isWritable() >> true
         id() >> idOne
         closeFuture() >> futureOne
         1 * writeAndFlush({ CasualNWMessage<DomainDiscoveryTopologyUpdateMessage> msg ->
            Objects.requireNonNull(msg, 'msg can not be null')
            if(! (msg.getMessage() instanceof DomainDiscoveryTopologyUpdateMessage)){
               throw new RuntimeException('expected type DomainDiscoveryTopologyUpdateMessage')
            }
            def topologyMessage = (DomainDiscoveryTopologyUpdateMessage)msg.getMessage()
            assert topologyMessage.getExecution() == execution
         })
      }
      def idTwo = Mock(ChannelId)
      def futureTwo = Mock(ChannelFuture)
      def channelTwo = Mock(Channel){
         isWritable() >> true
         id() >> idTwo
         closeFuture() >> futureTwo
         2 * writeAndFlush({ CasualNWMessage<DomainDiscoveryTopologyUpdateMessage> msg ->
            Objects.requireNonNull(msg, 'msg can not be null')
            if(! (msg.getMessage() instanceof DomainDiscoveryTopologyUpdateMessage)){
               throw new RuntimeException('expected type DomainDiscoveryTopologyUpdateMessage')
            }
            def topologyMessage = (DomainDiscoveryTopologyUpdateMessage)msg.getMessage()
            assert topologyMessage.getExecution() == execution
         })
      }
      InboundTopologyUpdateContext.add(channelOne)
      InboundTopologyUpdateContext.add(channelTwo)
      when:
      InboundTopologyUpdateContext.sendTopologyUpdate(execution)
      InboundTopologyUpdateContext.remove(channelOne)
      // this time we only write to channelTwo since channelOne was removed
      InboundTopologyUpdateContext.sendTopologyUpdate(execution)
      InboundTopologyUpdateContext.clear()
      // results in zero writes since there are no channels left
      InboundTopologyUpdateContext.sendTopologyUpdate(execution)
      then:
      noExceptionThrown()
   }
}
