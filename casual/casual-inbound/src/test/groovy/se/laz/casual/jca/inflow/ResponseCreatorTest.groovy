/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow

import jakarta.resource.spi.work.WorkEvent
import jakarta.resource.spi.work.WorkException
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.api.xa.XID
import se.laz.casual.jca.inflow.work.CasualServiceCallWork
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import spock.lang.Specification

import javax.transaction.xa.Xid

class ResponseCreatorTest extends Specification
{
   def "normal service call and response"()
   {
      given:
      CasualServiceCallReplyMessage replyMessage = Mock(CasualServiceCallReplyMessage) {
         getError() >> ErrorState.OK
      }
      UUID correlation = UUID.randomUUID()
      CasualNWMessage<CasualServiceCallReplyMessage> response = CasualNWMessageImpl<CasualServiceCallReplyMessage>.of(correlation, replyMessage)
      CasualServiceCallRequestMessage requestMessage = Mock(CasualServiceCallRequestMessage)
      CasualServiceCallWork serviceCallWork = new CasualServiceCallWork(correlation, requestMessage)
      serviceCallWork.response = response
      WorkEvent event = Mock(WorkEvent){
         getWork() >> serviceCallWork
         getException() >> null
      }
      UUID serviceCallExecution = UUID.randomUUID()
      def parentName = 'Bob'
      def serviceName = 'dinner'
      def serviceXid = Mock(Xid)
      WorkResponseContext context = WorkResponseContext.createBuilder()
                                                       .withCorrelationId(correlation)
                                                       .withExecution(serviceCallExecution)
                                                       .withParentName(parentName)
                                                       .withServiceName(serviceName)
                                                       .withXid(serviceXid)
                                                       .build()
      when:
      ServiceCallResult result = ResponseCreator.create(event, context, false)
      then:
      result.result() == response
      result.resultCode() == ErrorState.OK
   }

   def "exceptional service call and response"()
   {
      given:
      WorkException exception = Mock(WorkException)
      UUID correlation = UUID.randomUUID()
      CasualNWMessage<CasualServiceCallReplyMessage> response = null
      CasualServiceCallRequestMessage requestMessage = Mock(CasualServiceCallRequestMessage)
      CasualServiceCallWork serviceCallWork = new CasualServiceCallWork(correlation, requestMessage)
      serviceCallWork.response = response
      WorkEvent event = Mock(WorkEvent){
         getWork() >> serviceCallWork
         getException() >> exception
      }
      UUID serviceCallExecution = UUID.randomUUID()
      def parentName = 'Bob'
      def serviceName = 'dinner'
      def serviceXid = XID.NULL_XID
      WorkResponseContext context = WorkResponseContext.createBuilder()
              .withCorrelationId(correlation)
              .withExecution(serviceCallExecution)
              .withParentName(parentName)
              .withServiceName(serviceName)
              .withXid(serviceXid)
              .build()
      when:
      ServiceCallResult result = ResponseCreator.create(event, context, false)
      then:
      result.result().getCorrelationId() == correlation
      result.result().getMessage().getExecution() == serviceCallExecution
      result.result().getMessage().getXid() == serviceXid
      result.resultCode() == ErrorState.TPESYSTEM
      result.result().getMessage().getTransactionState() == TransactionState.ROLLBACK_ONLY
   }

   def "normal tpnoreply service call"()
   {
      given:
      UUID correlation = UUID.randomUUID()
      CasualNWMessage<CasualServiceCallReplyMessage> response = CasualNWMessageImpl<CasualServiceCallReplyMessage>.of(correlation, null)
      CasualServiceCallRequestMessage requestMessage = Mock(CasualServiceCallRequestMessage)
      CasualServiceCallWork serviceCallWork = new CasualServiceCallWork(correlation, requestMessage, true)
      serviceCallWork.response = response
      WorkEvent event = Mock(WorkEvent){
         getWork() >> serviceCallWork
         getException() >> null
      }
      UUID serviceCallExecution = UUID.randomUUID()
      def parentName = 'Bob'
      def serviceName = 'dinner'
      def serviceXid = Mock(Xid)
      WorkResponseContext context = WorkResponseContext.createBuilder()
              .withCorrelationId(correlation)
              .withExecution(serviceCallExecution)
              .withParentName(parentName)
              .withServiceName(serviceName)
              .withXid(serviceXid)
              .build()
      when:
      ServiceCallResult result = ResponseCreator.create(event, context, true)
      then:
      result.result() == null
      result.resultCode() == ErrorState.OK
   }

   def "exceptional tpnoreply service call"()
   {
      given:
      WorkException exception = Mock(WorkException)
      UUID correlation = UUID.randomUUID()
      CasualNWMessage<CasualServiceCallReplyMessage> response = CasualNWMessageImpl<CasualServiceCallReplyMessage>.of(correlation, null)
      CasualServiceCallRequestMessage requestMessage = Mock(CasualServiceCallRequestMessage)
      CasualServiceCallWork serviceCallWork = new CasualServiceCallWork(correlation, requestMessage, true)
      serviceCallWork.response = response
      WorkEvent event = Mock(WorkEvent){
         getWork() >> serviceCallWork
         getException() >> exception
      }
      UUID serviceCallExecution = UUID.randomUUID()
      def parentName = 'Bob'
      def serviceName = 'dinner'
      def serviceXid = Mock(Xid)
      WorkResponseContext context = WorkResponseContext.createBuilder()
              .withCorrelationId(correlation)
              .withExecution(serviceCallExecution)
              .withParentName(parentName)
              .withServiceName(serviceName)
              .withXid(serviceXid)
              .build()
      when:
      ServiceCallResult result = ResponseCreator.create(event, context, true)
      then:
      result.result() == null
      result.resultCode() == ErrorState.TPESYSTEM
   }
}
