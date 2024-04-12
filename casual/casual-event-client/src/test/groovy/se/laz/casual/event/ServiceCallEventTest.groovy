/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.util.PrettyPrinter
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.transaction.xa.Xid
import java.time.Instant
import java.time.temporal.ChronoUnit

class ServiceCallEventTest extends Specification
{
   @Shared def service1 = 'Service One'
   @Shared def service2 = 'Service Two'
   @Shared def parent1 = 'Elvira'
   @Shared def parent2 = 'Elvis'
   @Shared def execution1 = UUID.randomUUID()
   @Shared def execution2 = UUID.randomUUID()
   @Shared def pid1 = 128
   @Shared def pid2 = 256
   @Shared def transactionId1 = Mock(Xid)
   @Shared def transactionId2 = Mock(Xid)
   @Shared def pending1 = 3
   @Shared def pending2 = 14
   @Shared def code1 = ErrorState.OK
   @Shared def code2 = ErrorState.TPENOENT
   @Shared def order1 = Order.SEQUENTIAL
   @Shared def order2 = Order.CONCURRENT

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
              .withService(service1)
              .withParent(parent1)
              .withPID(pid1)
              .withExecution(execution1)
              .withTransactionId(transactionId1)
              .withPending( pending1 )
      beforeStart = Instant.now()
      builder.start()
      afterStart = Instant.now()
      beforeEnd = afterStart
      builder.withCode(code1)
              .withOrder(order1)
              .end()
      afterEnd = Instant.now()
      instance = builder.build()

      then:
      instance.getService() == service1
      instance.getParent().get() == parent1
      instance.getPid() == pid1
      instance.getExecution() == PrettyPrinter.casualStringify(execution1)
      instance.getTransactionId() == PrettyPrinter.casualStringify(transactionId1)
      instance.getCode() == code1.name()
      instance.getOrder() == order1.getValue()

      instance.getPending() == pending1

      instance.getStart() >= ChronoUnit.MICROS.between(Instant.EPOCH, beforeStart)
      instance.getStart() <= ChronoUnit.MICROS.between(Instant.EPOCH, afterStart)
      instance.getEnd() >= ChronoUnit.MICROS.between(Instant.EPOCH, beforeEnd)
      instance.getEnd() <= ChronoUnit.MICROS.between(Instant.EPOCH, afterEnd)
   }

   @Unroll
   def "Build, null checks"()
   {
      given:
      ServiceCallEvent.Builder builder = ServiceCallEvent.createBuilder(  )
              .withService(service)
              .withParent(parent)
              .withExecution(execution)
              .withTransactionId(transaction)
              .withCode(code)
              .withOrder(order)
      if( start )
      {
         builder.start()
      }
      if( end )
      {
         builder.end()
      }

      when:
      builder.build()

      then:
      thrown NullPointerException

      where:
      service  | parent  | execution  | transaction    | start | end   | code  | order
      null     | parent1 | execution1 | transactionId1 | true  | true  | code1 | order1
      service1 | null    | execution1 | transactionId1 | true  | true  | code1 | order1
      service1 | parent1 | null       | transactionId1 | true  | true  | code1 | order1
      service1 | parent1 | execution1 | null           | true  | true  | code1 | order1
      service1 | parent1 | execution1 | transactionId1 | false | true  | code1 | order1
      service1 | parent1 | execution1 | transactionId1 | true  | false | code1 | order1
      service1 | parent1 | execution1 | transactionId1 | true  | true  | null  | order1
      service1 | parent1 | execution1 | transactionId1 | true  | true  | code1 | null
   }

   def 'identity'()
   {
      when:
      ServiceCallEvent firstEntry = createEntry([service: service1, parent: parent1, pid: pid1, execution: execution1, transactionId: transactionId1, pending: pending1, code: code1, order: order1])
      ServiceCallEvent secondEntry = createEntry([service: service2, parent: parent2, pid: pid2, execution: execution2, transactionId: transactionId2, pending: pending2, code: code2, order: order2])

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
              .withService(service1)
              .withParent(parent1)
              .withPID(pid1)
              .withExecution(execution1)
              .withTransactionId(transactionId1)
              .withOrder(order1)

      when:
      Thread.sleep( 1 )
      builder.start(  )
      builder.withCode( code1 )
      Thread.sleep( 1 )
      builder.end( )
      ServiceCallEvent instance = builder.build(  )

      then:
      instance.getPending(  ) >= 1000
      instance.getStart(  ) < instance.getEnd(  )
   }
}
