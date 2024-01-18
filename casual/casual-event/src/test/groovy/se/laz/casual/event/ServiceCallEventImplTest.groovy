/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEventImpl
import spock.lang.Specification

import javax.transaction.xa.Xid

class ServiceCallEventImplTest extends Specification
{
   def 'identity'()
   {
      when:
      ServiceCallEventImpl firstEntry = createEntry([service:'service one', parent:'Elvira', pid: 13245, execution: UUID.randomUUID(), transactionId: Mock(Xid), start: 1, end: 42, pending: 0, code: ErrorState.OK.value, order: Order.SEQUENTIAL])
      ServiceCallEventImpl secondEntry = createEntry([service:'service two', parent:'Elvis', pid: 132456, execution: UUID.randomUUID(), transactionId: Mock(Xid), start: 43, end: 142, pending: 14, code: ErrorState.TPENOENT.value, order: Order.CONCURRENT])
      then:
      firstEntry == firstEntry
      firstEntry != secondEntry
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