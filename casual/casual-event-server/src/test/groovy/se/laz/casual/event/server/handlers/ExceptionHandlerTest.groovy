/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server.handlers

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.group.ChannelGroup
import se.laz.casual.api.CasualRuntimeException
import se.laz.casual.api.external.json.JsonProviderFactory
import se.laz.casual.event.server.messages.ConnectReply
import se.laz.casual.event.server.messages.ConnectReplyMessage
import spock.lang.Specification

class ExceptionHandlerTest extends Specification
{
   def 'removes client'()
   {
      given:
      Channel actualChannel = Mock(Channel)
      ChannelHandlerContext ctx = Mock(ChannelHandlerContext){
         2 * channel() >> actualChannel
         1 * close()
      }
      ChannelGroup clients = Mock(ChannelGroup){
         1 * remove(actualChannel)
      }
      ExceptionHandler exceptionHandler = ExceptionHandler.of(clients)
      when:
      exceptionHandler.exceptionCaught(ctx, new CasualRuntimeException("Ooopsie"))
      println("${JsonProviderFactory.getJsonProvider().toJson(ConnectReplyMessage.of(ConnectReply.CONNECT_REPLY))}")
      then:
      noExceptionThrown()
   }
}
