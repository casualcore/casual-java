/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server.handlers

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.group.ChannelGroup
import io.netty.util.CharsetUtil
import se.laz.casual.api.CasualTypeException
import se.laz.casual.event.server.messages.ConnectRequestMessage
import spock.lang.Specification

class FromJSONLogonDecoderTest extends Specification
{
   def 'failed logon'()
   {
      given:
      def actualChannel = Mock(Channel)
      def clients = Mock(ChannelGroup){
         0 * add(actualChannel)
      }
      def ctx = Mock(ChannelHandlerContext){
         channel() >> actualChannel
         0 * fireChannelRead(_ as ConnectRequestMessage)
      }
      def logonMessage = '{"message" : "this will fail"}'
      def buffer = Mock(ByteBuf){
         toString(CharsetUtil.UTF_8) >> logonMessage
      }
      def decoder = FromJSONLogonDecoder.of(clients)
      when:
      decoder.channelRead0(ctx, buffer)
      then:
      thrown(CasualTypeException)
   }

   def 'ok logon'()
   {
      given:
      def actualChannel = Mock(Channel)
      def clients = Mock(ChannelGroup){
         1 * add(actualChannel)
      }
      def ctx = Mock(ChannelHandlerContext){
         channel() >> actualChannel
         1 * fireChannelRead(_ as ConnectRequestMessage)
      }
      def logonMessage = '{"message" : "HELLO"}'
      def buffer = Mock(ByteBuf){
         toString(CharsetUtil.UTF_8) >> logonMessage
      }
      def decoder = FromJSONLogonDecoder.of(clients)
      when:
      decoder.channelRead0(ctx, buffer)
      then:
      noExceptionThrown()
   }
}
