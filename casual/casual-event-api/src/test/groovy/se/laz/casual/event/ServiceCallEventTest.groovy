/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.util.PrettyPrinter
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import spock.lang.Specification

import javax.transaction.xa.Xid

class ServiceCallEventTest extends Specification
{
   def firstService = 'Service One'
   def secondService = 'Service Two'
   def firstParent = 'Elvira'
   def secondParent = 'Elvis'
   def firstExecution = UUID.randomUUID()
   def secondExecution = UUID.randomUUID()
   def firstPID = 128
   def secondPID = 256
   def firstTransactionId = Mock(Xid)
   def secondTransactionId = Mock(Xid)
   def firstStart = 1
   def secondStart = 43
   def firstEnd = 42
   def secondEnd = 128
   def firstPending = 0
   def secondPending = 14
   def firstCode = ErrorState.OK
   def secondCode = ErrorState.TPENOENT
   def firstOrder = Order.SEQUENTIAL
   def secondOrder = Order.CONCURRENT
    
   def 'identity'()
   {
      given:

      when:
      ServiceCallEvent firstEntry = createEntry([service: firstService, parent: firstParent, pid: firstPID, execution: firstExecution, transactionId: firstTransactionId, start: firstStart, end: firstEnd, pending: firstPending, code: firstCode, order: firstOrder])
      ServiceCallEvent secondEntry = createEntry([service: secondService, parent: secondParent, pid: secondPID, execution: secondExecution, transactionId: secondTransactionId, start: secondStart, end: secondEnd, pending: secondPending, code: secondCode, order: secondOrder])
      then:
      firstEntry == firstEntry
      firstEntry != secondEntry

      firstEntry.hashCode() == firstEntry.hashCode()
      firstEntry.hashCode() != secondEntry.hashCode()

      firstEntry.getService() == firstService
      firstEntry.getParent().get() == firstParent
      firstEntry.getPid() == firstPID
      firstEntry.getExecution() == PrettyPrinter.casualStringify(firstExecution)
      firstEntry.getTransactionId() == PrettyPrinter.casualStringify(firstTransactionId)
      firstEntry.getStart() == firstStart
      firstEntry.getEnd() == firstEnd
      firstEntry.getPending() == firstPending
      firstEntry.getCode() == firstCode.name()
      firstEntry.getOrder() == firstOrder.getValue()

      secondEntry.getService() == secondService
      secondEntry.getParent().get() == secondParent
      secondEntry.getPid() == secondPID
      secondEntry.getExecution() == PrettyPrinter.casualStringify(secondExecution)
      secondEntry.getTransactionId() == PrettyPrinter.casualStringify(secondTransactionId)
      secondEntry.getStart() == secondStart
      secondEntry.getEnd() == secondEnd
      secondEntry.getPending() == secondPending
      secondEntry.getCode() == secondCode.name()
      secondEntry.getOrder() == secondOrder.getValue()
   }

   def createEntry(data)
   {
      return ServiceCallEvent.createBuilder()
              .withService(data.service)
              .withParent(data.parent)
              .withPID(data.pid)
              .withExecution(data.execution)
              .withTransactionId(data.transactionId)
              .withStart(data.start)
              .withEnd(data.end)
              .withPending(data.pending)
              .withCode(data.code)
              .withOrder(data.order)
              .build()
   }
}