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
      given:
      UUID domainId = UUID.randomUUID()
      when:
      ServiceCallEventStore handler = ServiceCallEventStoreFactory.getStore(domainId)
      then:
      handler.getClass() == CasualServiceCallEventStore.class
   }

   def "get same instance with multiple calls to getHandler with same domain id."()
   {
      given:
      UUID domainId = UUID.randomUUID()
      when:
      ServiceCallEventStore handler1 = ServiceCallEventStoreFactory.getStore(domainId)
      ServiceCallEventStore handler2 = ServiceCallEventStoreFactory.getStore(domainId)
      then:
      handler1.equals( handler2 )
   }

   def "get different instance with multiple calls to getHandler with different domain id."()
   {
      given:
      UUID domainId1 = UUID.randomUUID()
      UUID domainId2 = UUID.randomUUID()
      when:
      ServiceCallEventStore handler1 = ServiceCallEventStoreFactory.getStore(domainId1)
      ServiceCallEventStore handler2 = ServiceCallEventStoreFactory.getStore(domainId2)
      then:
      !handler1.equals(handler2)
   }
}
