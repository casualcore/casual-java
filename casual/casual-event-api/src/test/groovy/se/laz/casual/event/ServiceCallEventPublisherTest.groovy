/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event


import se.laz.casual.jca.RuntimeInformation
import spock.lang.Shared
import spock.lang.Specification

class ServiceCallEventPublisherTest extends Specification
{
   @Shared
   ServiceCallEvent event = Mock(ServiceCallEvent)

   def 'event server not started, event will not be published'()
   {
      given:
      ServiceCallEventStore eventHandler = Mock(ServiceCallEventStore){
         0 * put(_)
      }
      ServiceCallEventPublisher publisher = ServiceCallEventPublisher.of(eventHandler)
      when:
      publisher.post(event)
      then:
      noExceptionThrown()
   }

   def 'event server started, event will be published'()
   {
      given:
      RuntimeInformation.setEventServerStarted(true)
      ServiceCallEventStore eventHandler = Mock(ServiceCallEventStore){
         1 * put(_)
      }
      ServiceCallEventPublisher publisher = ServiceCallEventPublisher.of(eventHandler)
      when:
      publisher.post(event)
      then:
      noExceptionThrown()
   }
}
