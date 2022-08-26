/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.entitites

import se.laz.casual.connection.caller.entities.CacheEntry
import se.laz.casual.connection.caller.entities.CacheEntryWithHops
import se.laz.casual.connection.caller.entities.CacheEntryWithHopsComparator
import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry
import se.laz.casual.connection.caller.entities.ConnectionFactoryProducer
import se.laz.casual.jca.DomainId
import spock.lang.Specification

class CacheEntryWithHopsComparatorTest extends Specification
{
   def 'assure order'()
   {
      given:
      DomainId domainIdOne = DomainId.of(UUID.randomUUID())
      ConnectionFactoryEntry connectionFactoryEntryOne = ConnectionFactoryEntry.of(Mock(ConnectionFactoryProducer))
      DomainId domainIdTwo = DomainId.of(UUID.randomUUID())
      ConnectionFactoryEntry connectionFactoryEntryTwo = ConnectionFactoryEntry.of(Mock(ConnectionFactoryProducer))
      CacheEntry cacheEntryOne = CacheEntry.of(domainIdOne, connectionFactoryEntryOne)
      CacheEntry cacheEntryTwo = CacheEntry.of(domainIdTwo, connectionFactoryEntryTwo)

      CacheEntryWithHops firstEntry = CacheEntryWithHops.of(cacheEntryOne, 0)
      CacheEntryWithHops secondEntry = CacheEntryWithHops.of(cacheEntryTwo, 1)
      CacheEntryWithHops thirdEntry = CacheEntryWithHops.of(cacheEntryOne, 2)
      CacheEntryWithHops fourthEntry = CacheEntryWithHops.of(cacheEntryOne, 3)

      List<CacheEntryWithHops> entries = new ArrayList<>()
      entries.add(fourthEntry)
      entries.add(firstEntry)
      entries.add(secondEntry)
      entries.add(thirdEntry)

      when:
      Collections.sort(entries, CacheEntryWithHopsComparator.of())

      then:
      assertOrder(entries, [firstEntry, secondEntry, thirdEntry, fourthEntry])
   }

   void assertOrder(ArrayList<CacheEntryWithHops> result, ArrayList<CacheEntryWithHops> expected)
   {
      assert(result.size() == expected.size())
      for(int i=0; i<result.size(); ++i)
      {
         assert(result.get(i) == expected.get(i))
      }
   }
}
