/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import io.netty.channel.ChannelHandlerContext
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.conversation.Duplex
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.conversation.Request
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class ConversationMessageHandlerTest extends Specification
{
   @Shared
   def conversationMessageStorage = ConversationMessageStorageImpl.of()
   @Shared
   def instance = ConversationMessageHandler.of(conversationMessageStorage)
   @Shared
   def corrId = UUID.randomUUID()

   def 'failed construction'()
   {
      when:
      ConversationMessageHandler.of(null)
      then:
      thrown(NullPointerException)
   }

   def 'ok'()
   {
      given:
      CasualNWMessage<Request> requestMessage = CasualNWMessageImpl.of(corrId, Request.createBuilder()
              .setServiceBuffer(ServiceBuffer.of(JsonBuffer.of('"Hello":"Bob!"')))
              .setExecution(UUID.randomUUID())
              .setDuplex(Duplex.SEND)
              .build())
      when:
      instance.channelRead0(Mock(ChannelHandlerContext), requestMessage)
      then:
      noExceptionThrown()
      conversationMessageStorage.size(corrId) == 1
      and:
      when:
      def storedMsg = conversationMessageStorage.nextMessage(corrId).get()
      then:
      storedMsg == requestMessage
   }

   def 'producer/consumer'()
   {
      given:
      CasualNWMessage<Request> requestMessage = CasualNWMessageImpl.of(corrId, Request.createBuilder()
              .setServiceBuffer(ServiceBuffer.of(JsonBuffer.of('"Hello":"Bob!"')))
              .setExecution(UUID.randomUUID())
              .setDuplex(Duplex.SEND)
              .build())
      CompletableFuture<CasualNWMessage<Request>> future = new CompletableFuture<>()
      when:
      def size = conversationMessageStorage.size(corrId)
      then:
      size == 0
      when:
      CompletableFuture.supplyAsync({future.complete(conversationMessageStorage.takeFirst(corrId))})
      instance.channelRead0(Mock(ChannelHandlerContext), requestMessage)
      def storedMessage = future.join()
      then:
      storedMessage == requestMessage
      conversationMessageStorage.size(corrId) == 0
   }

}
