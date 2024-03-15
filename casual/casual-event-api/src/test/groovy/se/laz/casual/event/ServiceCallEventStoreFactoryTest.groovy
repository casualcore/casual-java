/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event

import spock.lang.Specification

class ServiceCallEventStoreFactoryTest extends Specification
{
   def 'gets the default handler'()
   {
      when:
      ServiceCallEventStore handler = ServiceCallEventHandlerFactory.getHandler()
      then:
      handler.getClass() == CasualServiceCallEventStore.class
   }
}
