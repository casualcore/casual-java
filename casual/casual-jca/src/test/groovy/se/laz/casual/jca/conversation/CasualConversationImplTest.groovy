/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.conversation

import se.laz.casual.api.Conversation
import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.ConversationReturn
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.conversation.Duplex
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.api.xa.XID
import se.laz.casual.event.server.EventServer
import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.jca.CasualManagedConnection
import se.laz.casual.jca.CasualManagedConnectionFactory
import se.laz.casual.jca.CasualResourceAdapter
import se.laz.casual.jca.CasualResourceManager
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.conversation.Request
import spock.lang.Shared
import spock.lang.Specification

import jakarta.resource.spi.work.WorkManager
import java.util.concurrent.CompletableFuture

class CasualConversationImplTest extends Specification
{
   @Shared Conversation instance
   @Shared ConversationConnectCaller conversationConnectCaller
   @Shared CasualManagedConnection connection
   @Shared NetworkConnection networkConnection
   @Shared UUID corrId
   @Shared UUID conversationExecution
   @Shared String serviceName
   @Shared JsonBuffer message
   @Shared JsonBuffer replyMsg
   @Shared CasualNWMessageImpl<Request> expectedSendRequest
   @Shared CasualNWMessageImpl<Request> expectedReceiveRequest
   @Shared CasualNWMessageImpl<Request> actualSendRequest
   @Shared ConversationDirection conversationDirection = ConversationDirection.RECEIVE
   @Shared mcf
   @Shared ra
   @Shared workManager

   def setup()
   {
      initialiseParameters()

      workManager = Mock(WorkManager)
      ra = new CasualResourceAdapter(Mock(EventServer))
      ra.workManager = workManager
      mcf = Mock(CasualManagedConnectionFactory)
      networkConnection = Mock(NetworkConnection)

      connection = new CasualManagedConnection( mcf )
      connection.networkConnection =  networkConnection

      CasualResourceManager.getInstance().remove(XID.NULL_XID)
      connection.getXAResource().start( XID.NULL_XID, 0 )
      CasualResourceManager.getInstance().remove(XID.NULL_XID)

      conversationConnectCaller = ConversationConnectCaller.of(connection)
      instance = CasualConversationImpl.of(connection, conversationDirection, corrId, conversationExecution)

      initialiseExpectedRequests()
      initializeReplies()
   }

   def initialiseParameters()
   {
      conversationExecution = UUID.randomUUID()
      corrId = UUID.randomUUID()
      serviceName = "hello bob"
      message = JsonBuffer.of('"Hello":"Bob!"')
      replyMsg = JsonBuffer.of('"Hello":"Jane"')
   }

   def initialiseExpectedRequests()
   {
      expectedSendRequest = CasualNWMessageImpl.of(corrId, Request.createBuilder()
              .setServiceBuffer(ServiceBuffer.of(message))
              .setExecution(conversationExecution)
              .setDuplex(Duplex.SEND)
              .build())
   }

   def initializeReplies()
   {
      expectedReceiveRequest = CasualNWMessageImpl.of(corrId, Request.createBuilder()
              .setServiceBuffer(ServiceBuffer.of(replyMsg))
              .setResultCode(ErrorState.OK.getValue())
              .setDuplex(Duplex.SEND)
              .setExecution(conversationExecution)
              .build())
   }

   def 'tprecv, control handed over, tpsend, tpsend again and hand over control - ready to recv again'()
   {
      given:
      1 * networkConnection.receive(corrId) >> {
         CompletableFuture<CasualNWMessage<Request>> future = new CompletableFuture<>()
         future.complete(expectedReceiveRequest)
         return future
      }
      when:
      ConversationReturn<CasualBuffer> msg = instance.tprecv()
      then:
      msg.errorState.get() == ErrorState.OK
      instance.isSending()
      instance.isDirectionSwitched()
      msg.replyBuffer.getBytes() == replyMsg.getBytes()
      msg.duplex == Duplex.SEND
      when:
      2 * networkConnection.send(_) >> {
         CasualNWMessageImpl<Request> input ->
            actualSendRequest = input
      }
      instance.tpsend(message, false)
      then:
      JsonBuffer.of(expectedSendRequest.getMessage().getServiceBuffer().getPayload()) == JsonBuffer.of(actualSendRequest.getMessage().getServiceBuffer().getPayload())
      !instance.isDirectionSwitched()
      instance.isSending()
      when:
      instance.tpsend(message, true)
      then:
      JsonBuffer.of(expectedSendRequest.getMessage().getServiceBuffer().getPayload()) == JsonBuffer.of(actualSendRequest.getMessage().getServiceBuffer().getPayload())
      instance.isReceiving()
      instance.isDirectionSwitched()
   }

   def 'disconnect'()
   {
      given:
      1 * networkConnection.send(_)
      when:
      instance.tpdiscon()
      then:
      noExceptionThrown()
   }

}
