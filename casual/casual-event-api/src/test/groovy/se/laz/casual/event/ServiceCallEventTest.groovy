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
   def firstPending = 3
   def secondPending = 14
   def firstCode = ErrorState.OK
   def secondCode = ErrorState.TPENOENT
   def firstOrder = Order.SEQUENTIAL
   def secondOrder = Order.CONCURRENT

   def "Create then get."()
   {
      given:
      ServiceCallEvent instance
      Instant beforeStart
      Instant afterStart
      Instant beforeEnd
      Instant afterEnd

      when:
      ServiceCallEvent.Builder builder = ServiceCallEvent.createBuilder(  )
              .withService(firstService)
              .withParent(firstParent)
              .withPID(firstPID)
              .withExecution(firstExecution)
              .withTransactionId(firstTransactionId)
              .withPending( firstPending )
      beforeStart = Instant.now()
      builder.start()
      afterStart = Instant.now()
      beforeEnd = afterStart
      builder.withCode(firstCode)
              .withOrder(firstOrder)
              .end()
      afterEnd = Instant.now()
      instance = builder.build()

      then:
      instance.getService() == firstService
      instance.getParent().get() == firstParent
      instance.getPid() == firstPID
      instance.getExecution() == PrettyPrinter.casualStringify(firstExecution)
      instance.getTransactionId() == PrettyPrinter.casualStringify(firstTransactionId)
      instance.getCode() == firstCode.name()
      instance.getOrder() == firstOrder.getValue()

      instance.getPending() == firstPending

      instance.getStart() >= ChronoUnit.MICROS.between(Instant.EPOCH, beforeStart)
      instance.getStart() <= ChronoUnit.MICROS.between(Instant.EPOCH, afterStart)
      instance.getEnd() >= ChronoUnit.MICROS.between(Instant.EPOCH, beforeEnd)
      instance.getEnd() <= ChronoUnit.MICROS.between(Instant.EPOCH, afterEnd)
   }

   def 'identity'()
   {
      when:
      ServiceCallEvent firstEntry = createEntry([service: firstService, parent: firstParent, pid: firstPID, execution: firstExecution, transactionId: firstTransactionId, start: firstStart, end: firstEnd, pending: firstPending, code: firstCode, order: firstOrder])
      ServiceCallEvent secondEntry = createEntry([service: secondService, parent: secondParent, pid: secondPID, execution: secondExecution, transactionId: secondTransactionId, start: secondStart, end: secondEnd, pending: secondPending, code: secondCode, order: secondOrder])

      then:
      firstEntry == firstEntry
      firstEntry != secondEntry

      firstEntry.hashCode() == firstEntry.hashCode()
      firstEntry.hashCode() != secondEntry.hashCode()
   }

   def createEntry(data)
   {
      return ServiceCallEvent.createBuilder()
              .withService(data.service)
              .withParent(data.parent)
              .withPID(data.pid)
              .withExecution(data.execution)
              .withTransactionId(data.transactionId)
              .start()
              .end()
              .withPending(data.pending)
              .withCode(data.code)
              .withOrder(data.order)
              .build()
   }

   def "Create event with small pauses."()
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
