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
import java.time.Instant
import java.time.temporal.ChronoUnit

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
   def firstStart = Instant.now()
   def secondStart = Instant.now()
   def firstEnd = Instant.now()
   def secondEnd = Instant.now()
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
      firstEntry.getStart() == ChronoUnit.MICROS.between(Instant.EPOCH, firstStart)
      firstEntry.getEnd() == ChronoUnit.MICROS.between(Instant.EPOCH, firstEnd)
      firstEntry.getPending() == firstPending
      firstEntry.getCode() == firstCode.name()
      firstEntry.getOrder() == firstOrder.getValue()

      secondEntry.getService() == secondService
      secondEntry.getParent().get() == secondParent
      secondEntry.getPid() == secondPID
      secondEntry.getExecution() == PrettyPrinter.casualStringify(secondExecution)
      secondEntry.getTransactionId() == PrettyPrinter.casualStringify(secondTransactionId)
      secondEntry.getStart() == ChronoUnit.MICROS.between(Instant.EPOCH, secondStart)
      secondEntry.getEnd() == ChronoUnit.MICROS.between(Instant.EPOCH, secondEnd)
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

   def "Create event without caller explicitly using Instant."()
   {
      given:
      ServiceCallEvent.Builder builder = ServiceCallEvent.createBuilder(  )
              .withService(firstService)
              .withParent(firstParent)
              .withPID(firstPID)
              .withExecution(firstExecution)
              .withTransactionId(firstTransactionId)
              .withOrder(firstOrder)

      when:
      Thread.sleep( 1 )
      builder.start(  )
      builder.withCode( firstCode )
      Thread.sleep( 1 )
      builder.end( )
      ServiceCallEvent instance = builder.build(  )

      then:
      instance.getPending(  ) >= 1000
      instance.getStart(  ) < instance.getEnd(  )
   }
}
