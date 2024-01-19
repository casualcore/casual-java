/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.ServiceCallEventImpl
import spock.lang.Specification

import javax.transaction.xa.Xid

class ServiceCallEventImplTest extends Specification
{
   def firstService = 'Service One'
   def secondService = 'Service Two'
   def firstParent = 'Elvira'
   def secondParent = 'Elvis'
   def firstExecution = UUID.randomUUID()
   def secondExecution = UUID.randomUUID()
   def firstPid = 12345
   def secondPid = 123456
   def firstTransactionId = Mock(Xid)
   def secondTransactionId = Mock(Xid)
   def firstStart = 1
   def secondStart = 43
   def firstEnd = 42
   def secondEnd = 128
   def firstPending = 0
   def secondPending = 14
   def firstCode = ErrorState.OK.value
   def secondCode = ErrorState.TPENOENT.value
   def firstOrder = Order.SEQUENTIAL
   def secondOrder = Order.CONCURRENT
    
   def 'identity'()
   {
      given:

      when:
      ServiceCallEventImpl firstEntry = createEntry([service: firstService, parent: firstParent, pid: firstPid, execution: firstExecution, transactionId: firstTransactionId, start: firstStart, end: firstEnd, pending: firstPending, code: firstCode, order: firstOrder])
      ServiceCallEventImpl secondEntry = createEntry([service: secondService, parent: secondParent, pid: secondPid, execution: secondExecution, transactionId: secondTransactionId, start: secondStart, end: secondEnd, pending: secondPending, code: secondCode, order: secondOrder])
      then:
      firstEntry == firstEntry
      firstEntry != secondEntry

      firstEntry.getService() == firstService
      firstEntry.getParent().get() == firstParent
      firstEntry.getProcessId() == firstPid
      firstEntry.getExecution() == firstExecution
      firstEntry.getTransactionId() == firstTransactionId
      firstEntry.getStart() == firstStart
      firstEntry.getEnd() == firstEnd
      firstEntry.getPending() == firstPending
      firstEntry.getCode() == firstCode
      firstEntry.getOrder() == firstOrder

      secondEntry.getService() == secondService
      secondEntry.getParent().get() == secondParent
      secondEntry.getProcessId() == secondPid
      secondEntry.getExecution() == secondExecution
      secondEntry.getTransactionId() == secondTransactionId
      secondEntry.getStart() == secondStart
      secondEntry.getEnd() == secondEnd
      secondEntry.getPending() == secondPending
      secondEntry.getCode() == secondCode
      secondEntry.getOrder() == secondOrder
   }

   def 'hashcode'()
    {
      given:
      ServiceCallEventImpl firstEntry = createEntry([service: firstService, parent: firstParent, pid: firstPid, execution: firstExecution, transactionId: firstTransactionId, start: firstStart, end: firstEnd, pending: firstPending, code: firstCode, order: firstOrder])
      ServiceCallEventImpl secondEntry = createEntry([service: secondService, parent: secondParent, pid: secondPid, execution: secondExecution, transactionId: secondTransactionId, start: secondStart, end: secondEnd, pending: secondPending, code: secondCode, order: secondOrder])
      when:
      Map<ServiceCallEvent, Boolean> map = new HashMap<>()
      map.put(firstEntry, true)
      map.put(secondEntry, false)
      then:
      firstEntry == firstEntry
      firstEntry != secondEntry
      map.get(firstEntry) == true
      map.get(secondEntry) == false
   }

   def createEntry(data)
   {
      return ServiceCallEventImpl.createBuilder()
              .withService(data.service)
              .withParent(data.parent)
              .withPid(data.pid)
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
