/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.pool

import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry
import se.laz.casual.connection.caller.entities.Pool
import se.laz.casual.connection.caller.events.DomainGoneEvent
import se.laz.casual.connection.caller.events.NewDomainEvent
import se.laz.casual.jca.DomainId
import spock.lang.Specification

import javax.enterprise.event.Event

class PoolUpdaterTest extends Specification
{
   def 'new connection, new domain id, domain id gone, all gone'()
   {
      given:
      def pools = [:]
      ConnectionFactoryEntry connectionFactoryEntry = Mock(ConnectionFactoryEntry)
      def newDomainIds = [DomainId.of(UUID.randomUUID()), DomainId.of(UUID.randomUUID())]
      def expectedPool = Pool.of(connectionFactoryEntry, newDomainIds)
      def newDomain = Mock(Event){
         1 * fire(_) >> { NewDomainEvent event ->
            assert event.getPool() == Pool.of(connectionFactoryEntry, newDomainIds)
         }
      }
      def domainGone = Mock(Event) {
         0 * fire(_)
      }
      PoolUpdater instance = PoolUpdater.createBuilder()
              .withConnectionFactoryEntry(connectionFactoryEntry)
              .withDomainGone(domainGone)
              .withNewDomain(newDomain)
              .withDomainIds(newDomainIds)
              .withPools(pools)
              .build()
      when: // initial
      instance.update()
      then:
      pools.get(connectionFactoryEntry) == expectedPool
      when: // new domain ids
      def moreDomainIds = [DomainId.of(UUID.randomUUID()), DomainId.of(UUID.randomUUID())]
      newDomain = Mock(Event){
         1 * fire(_) >> { NewDomainEvent event ->
            assert event.getPool() == Pool.of(connectionFactoryEntry, moreDomainIds)
         }
      }
      def totalDomainIds =  moreDomainIds + newDomainIds
      expectedPool = Pool.of(connectionFactoryEntry, totalDomainIds)
      instance = PoolUpdater.createBuilder()
              .withConnectionFactoryEntry(connectionFactoryEntry)
              .withDomainGone(domainGone)
              .withNewDomain(newDomain)
              .withDomainIds(totalDomainIds)
              .withPools(pools)
              .build()
      instance.update()
      then:
      pools.get(connectionFactoryEntry) == expectedPool
      when: // one domain id gone
      DomainId removedId = totalDomainIds.removeLast()
      newDomain = Mock(Event){
         0 * fire(_)
      }
      domainGone = Mock(Event) {
         1 * fire(_) >> { DomainGoneEvent event ->
            assert event.getDomainId() == removedId
         }
      }
      expectedPool = Pool.of(connectionFactoryEntry, totalDomainIds)
      instance = PoolUpdater.createBuilder()
              .withConnectionFactoryEntry(connectionFactoryEntry)
              .withDomainGone(domainGone)
              .withNewDomain(newDomain)
              .withDomainIds(totalDomainIds)
              .withPools(pools)
              .build()
      instance.update()
      then:
      pools.get(connectionFactoryEntry) == expectedPool
      when: // all domain ids gone
      newDomain = Mock(Event){
         0 * fire(_)
      }
      domainGone = Mock(Event) {
         totalDomainIds.size() * fire(_) >> { DomainGoneEvent event ->
            assert totalDomainIds.contains(event.getDomainId())
         }
      }
      expectedPool = null
      instance = PoolUpdater.createBuilder()
              .withConnectionFactoryEntry(connectionFactoryEntry)
              .withDomainGone(domainGone)
              .withNewDomain(newDomain)
              .withDomainIds([])
              .withPools(pools)
              .build()
      instance.update()
      then:
      pools.get(connectionFactoryEntry) == expectedPool
   }
}
