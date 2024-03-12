/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event

import spock.lang.Shared
import spock.lang.Specification

class CasualServiceCallEventHandlerTest extends Specification
{
   @Shared
   CasualServiceCallEventHandler instance = new CasualServiceCallEventHandler()

   def 'can not post null event'()
   {
      given:
      ServiceCallEvent event = null
      when:
      instance.put(event)
      then:
      thrown(NullPointerException)
   }

   def 'posting and retrieving event'()
   {
      given:
      ServiceCallEvent event = Mock(ServiceCallEvent)
      when:
      instance.put(event)
      ServiceCallEvent consumedEvent = instance.take()
      then:
      consumedEvent == event
   }

}
