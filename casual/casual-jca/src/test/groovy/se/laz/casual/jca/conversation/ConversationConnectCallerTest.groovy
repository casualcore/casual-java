/*
 * Copyright (c) 2021 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.conversation

import jakarta.resource.spi.work.WorkManager
import se.laz.casual.api.Conversation
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.conversation.Duplex
import se.laz.casual.api.conversation.TpConnectReturn
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.xa.XID
import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.jca.CasualManagedConnection
import se.laz.casual.jca.CasualManagedConnectionFactory
import se.laz.casual.jca.CasualResourceAdapter
import se.laz.casual.jca.CasualResourceManager
import se.laz.casual.jca.DomainId
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.connection.CasualConnectionException
import se.laz.casual.network.inbound.ProtocolVersionValueHolder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.conversation.ConnectReply
import se.laz.casual.network.protocol.messages.conversation.ConnectRequest
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class ConversationConnectCallerTest extends Specification
{
   @Shared ConversationConnectCaller instance
   @Shared CasualManagedConnection connection
   @Shared NetworkConnection networkConnection
   @Shared UUID executionId
   @Shared UUID corrId
   @Shared DomainId domainOne = DomainId.of(UUID.randomUUID())
   @Shared String serviceName
   @Shared JsonBuffer message
   @Shared CasualNWMessageImpl<ConnectRequest> expectedConnectRequest
   @Shared CasualNWMessageImpl<ConnectReply> connectReplyOK
   @Shared CasualNWMessageImpl<ConnectReply> connectReplyOKUserCodeMissing
   @Shared CasualNWMessageImpl<ConnectReply> connectReplyFail
   @Shared CasualNWMessageImpl<ConnectRequest> actualConnectRequest
   @Shared mcf
   @Shared ra
   @Shared workManager
   int USER_CODE_MISSING = -1
   ProtocolVersionValueHolder protocolVersionValueHolder = ProtocolVersionValueHolder.of()

   def setup()
   {
      workManager = Mock(WorkManager)
      ra = new CasualResourceAdapter()
      ra.workManager = workManager
      mcf = Mock(CasualManagedConnectionFactory)
      networkConnection = Mock(NetworkConnection){
         getDomainId() >> domainOne
         getProtocolVersion() >> {protocolVersionValueHolder.get()}
      }

      connection = new CasualManagedConnection( mcf )
      connection.networkConnection =  networkConnection

      CasualResourceManager.getInstance().remove(domainOne, XID.NULL_XID)
      connection.getXAResource().start( XID.NULL_XID, 0 )
      CasualResourceManager.getInstance().remove(domainOne, XID.NULL_XID)

      instance = ConversationConnectCaller.of( connection )

      initialiseParameters()
      initialiseExpectedRequests()
      initialiseReplies()
   }

   def initialiseParameters()
   {
      executionId = UUID.randomUUID()
      serviceName = "echo"
      message = JsonBuffer.of( "{msg: \"hello echo service.\"}" )
   }

   def initialiseExpectedRequests()
   {
      expectedConnectRequest = CasualNWMessageImpl.of(corrId, ConnectRequest.createBuilder()
              .setExecution(executionId)
              .setServiceBuffer(ServiceBuffer.of(message))
              .setServiceName(serviceName)
              .setDuplex(Duplex.SEND)
              .setXid(connection.getCurrentXid())
              .build())
   }

   def initialiseReplies()
   {
      def connectReplyMsgOK = ConnectReply.createBuilder()
              .setExecution(executionId)
              .setResultCode(ErrorState.OK.getValue())
              .build()
      connectReplyOK = CasualNWMessageImpl.of(corrId, connectReplyMsgOK)
      def connectReplyMsgFail = ConnectReply.createBuilder()
              .setExecution(executionId)
              .setResultCode(ErrorState.TPENOENT.getValue())
              .build()
      connectReplyFail = CasualNWMessageImpl.of(corrId, connectReplyMsgFail)
      def connectReplyMsgUserCodeMissing = ConnectReply.createBuilder()
              .setExecution(executionId)
              .setResultCode(USER_CODE_MISSING)
              .build()
      connectReplyOKUserCodeMissing = CasualNWMessageImpl.of(corrId, connectReplyMsgUserCodeMissing)
   }

   def 'ok tpconnect'()
   {
      given:
      protocolVersionValueHolder.accept(protocolVersion)
      1 * networkConnection.request( _ ) >> {
         CasualNWMessageImpl<ConnectRequest> input ->
            actualConnectRequest = input
            return CompletableFuture.completedFuture(connectReplyOK)
      }
      when:
      TpConnectReturn connectReturn = instance.tpconnect(serviceName, message, Flag.of(AtmiFlags.TPSENDONLY))
      then:
      connectReturn.getErrorState() == ErrorState.OK
      when:
      Conversation conversation = connectReturn.getConversation().orElseThrow({ new RuntimeException('oopsie!')})
      then:
      conversation != null
      conversation.isSending()
      expectedConnectRequest.getMessage().serviceName == actualConnectRequest.getMessage().serviceName
      expectedConnectRequest.getMessage().timeout == actualConnectRequest.getMessage().timeout
      expectedConnectRequest.getMessage().parentName == actualConnectRequest.getMessage().parentName
      expectedConnectRequest.getMessage().parentName == actualConnectRequest.getMessage().parentName
      expectedConnectRequest.getMessage().xid == actualConnectRequest.getMessage().xid
      expectedConnectRequest.getMessage().serviceBuffer.bytes == actualConnectRequest.getMessage().serviceBuffer.bytes
      where:
      protocolVersion << ProtocolVersion.values()
   }

   def 'ok tpconnect - user code missing in reply'()
   {
      given:
      protocolVersionValueHolder.accept(protocolVersion)
      1 * networkConnection.request( _ ) >> {
         CasualNWMessageImpl<ConnectRequest> input ->
            actualConnectRequest = input
            return CompletableFuture.completedFuture(connectReplyOKUserCodeMissing)
      }
      when:
      TpConnectReturn connectReturn = instance.tpconnect(serviceName, message, Flag.of(AtmiFlags.TPSENDONLY))
      then:
      connectReturn.getErrorState() == ErrorState.OK
      when:
      Conversation conversation = connectReturn.getConversation().orElseThrow({ new RuntimeException('oopsie!')})
      then:
      conversation != null
      conversation.isSending()
      expectedConnectRequest.getMessage().serviceName == actualConnectRequest.getMessage().serviceName
      expectedConnectRequest.getMessage().timeout == actualConnectRequest.getMessage().timeout
      expectedConnectRequest.getMessage().parentName == actualConnectRequest.getMessage().parentName
      expectedConnectRequest.getMessage().parentName == actualConnectRequest.getMessage().parentName
      expectedConnectRequest.getMessage().xid == actualConnectRequest.getMessage().xid
      expectedConnectRequest.getMessage().serviceBuffer.bytes == actualConnectRequest.getMessage().serviceBuffer.bytes
      where:
      protocolVersion << ProtocolVersion.values()
   }

   def 'tpconnect TPFAIL'()
   {
      given:
      protocolVersionValueHolder.accept(protocolVersion)
      1 * networkConnection.request( _ ) >> {
         CasualNWMessageImpl<ConnectRequest> input ->
            throw new CasualConnectionException("network gone")
      }
      when:
      instance.tpconnect(serviceName, message, Flag.of(AtmiFlags.TPRECVONLY))
      then:
      thrown(CasualConnectionException)
      where:
      protocolVersion << ProtocolVersion.values()
   }

   def 'tpconnect TPFAIL not ok errorstate'()
   {
      given:
      protocolVersionValueHolder.accept(protocolVersion)
      1 * networkConnection.request( _ ) >> {
         CasualNWMessageImpl<ConnectRequest> input ->
            return CompletableFuture.completedFuture(connectReplyFail)
      }
      when:
      TpConnectReturn connectReturn = instance.tpconnect(serviceName, message, Flag.of(AtmiFlags.TPSENDONLY))
      then:
      connectReturn.getErrorState() == ErrorState.unmarshal(connectReplyFail.getMessage().getResultCode())
      where:
      protocolVersion << ProtocolVersion.values()
   }

}
