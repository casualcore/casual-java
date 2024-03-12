/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.jca.RuntimeInformation
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.time.Instant

class ServiceCallEventPublisherTest extends Specification
{
   @Shared
   Xid xid = Mock(Xid)
   @Shared
   UUID execution = UUID.randomUUID()
   @Shared
   String parentName = ""
   @Shared
   String serviceName = "fast-service"
   @Shared
   ErrorState code = ErrorState.OK
   @Shared
   long pendingMicroseconds = 100
   @Shared
   Instant start = Instant.now()
   @Shared
   Instant end = Instant.now()
   @Shared
   Order order = Order.CONCURRENT

   def 'event server not started, event will not be published'()
   {
      given:
      ServiceCallEventHandler eventHandler = Mock(ServiceCallEventHandler){
         0 * put(_)
      }
      ServiceCallEventPublisher publisher = ServiceCallEventPublisher.of(eventHandler)
      when:
      publisher.createAndPostEvent(xid, execution, parentName, serviceName, code, pendingMicroseconds, start, end, order)
      then:
      noExceptionThrown()
   }

   def 'event server started, event will be published'()
   {
      given:
      RuntimeInformation.setEventServerStarted(true)
      ServiceCallEventHandler eventHandler = Mock(ServiceCallEventHandler){
         1 * put(_)
      }
      ServiceCallEventPublisher publisher = ServiceCallEventPublisher.of(eventHandler)
      when:
      publisher.createAndPostEvent(xid, execution, parentName, serviceName, code, pendingMicroseconds, start, end, order)
      then:
      noExceptionThrown()
   }
}
