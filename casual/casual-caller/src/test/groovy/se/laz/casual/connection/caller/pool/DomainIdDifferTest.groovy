/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.pool

import se.laz.casual.jca.DomainId
import spock.lang.Specification

class DomainIdDifferTest extends Specification
{
   def 'no difference'()
   {
      given:
      DomainId first = DomainId.of(UUID.randomUUID())
      DomainId second = DomainId.of(UUID.randomUUID())
      DomainId third = DomainId.of(UUID.randomUUID())
      def firstList = [first, second, third]
      def secondList = [second, third, first]
      when:
      def result = DomainIdDiffer.of(firstList, secondList).diff()
      then:
      result.getLostDomainIds().isEmpty()
      result.getNewDomainIds().isEmpty()
   }

   def 'new domain'()
   {
      given:
      DomainId first = DomainId.of(UUID.randomUUID())
      DomainId second = DomainId.of(UUID.randomUUID())
      DomainId third = DomainId.of(UUID.randomUUID())
      DomainId newId = DomainId.of(UUID.randomUUID())
      def firstList = [first, second, third]
      def secondList = [second, third, newId, first]
      when:
      def result = DomainIdDiffer.of(firstList, secondList).diff()
      then:
      result.getLostDomainIds().isEmpty()
      result.getNewDomainIds() == [newId]
   }

   def 'lost domain'()
   {
      given:
      DomainId first = DomainId.of(UUID.randomUUID())
      DomainId second = DomainId.of(UUID.randomUUID())
      DomainId third = DomainId.of(UUID.randomUUID())
      def firstList = [first, second, third]
      def secondList = [second, third]
      when:
      def result = DomainIdDiffer.of(firstList, secondList).diff()
      then:
      result.getLostDomainIds() == [first]
      result.getNewDomainIds().isEmpty()
   }

   def 'new and lost domain'()
   {
      given:
      DomainId first = DomainId.of(UUID.randomUUID())
      DomainId second = DomainId.of(UUID.randomUUID())
      DomainId third = DomainId.of(UUID.randomUUID())
      DomainId newId = DomainId.of(UUID.randomUUID())
      def firstList = [first, second, third]
      def secondList = [second, newId, first]
      when:
      def result = DomainIdDiffer.of(firstList, secondList).diff()
      then:
      result.getLostDomainIds() == [third]
      result.getNewDomainIds() == [newId]
   }


}
