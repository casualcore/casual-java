/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound


import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.network.protocol.messages.conversation.Request
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class ConversationMessageStorageTest extends Specification
{
   @Shared
   def instance = ConversationMessageStorageImpl.of()

   def 'blocks on message retrieval'()
   {
      given:
      instance.clearAllConversations()
      CasualNWMessage<Request> requestMessage = Mock()
      UUID corrid = UUID.randomUUID()
      CompletableFuture<CasualNWMessage<Request>> future = new CompletableFuture<>()
      when:
      CompletableFuture.supplyAsync({future.complete(instance.takeFirst(corrid))})
      then:
      instance.size(corrid) == 0
      future.isDone() == false
      when:
      instance.put(corrid, requestMessage)
      def storedRequest = future.join()
      then:
      instance.size(corrid) == 0
      storedRequest == requestMessage
      future.isDone() == true
   }

   def 'non blocking retrieval'()
   {
      given:
      instance.clearAllConversations()
      CasualNWMessage<Request> requestMessage = Mock()
      UUID corrId = UUID.randomUUID()
      when:
      Optional<CasualNWMessage<Request>> storedMsg = instance.nextMessage(corrId)
      then:
      storedMsg.isPresent() == false
      instance.size(corrId) == 0
      when:
      instance.put(corrId, requestMessage)
      then:
      instance.size(corrId) == 1
      instance.numberOfConversations() == 1
      when:
      CasualNWMessage<Request> msg = instance.nextMessage(corrId).get()
      then:
      noExceptionThrown()
      msg == requestMessage
      instance.size(corrId) == 0
   }

}
