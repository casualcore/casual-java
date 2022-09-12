/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller

import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry
import se.laz.casual.connection.caller.entities.Pool
import se.laz.casual.connection.caller.pool.PoolManager
import se.laz.casual.jca.DomainId
import spock.lang.Specification

import javax.enterprise.event.Event

class PoolManagerTest extends Specification
{
   def 'pool updating with new instances'()
   {
      given:
      def domainIds = [DomainId.of(UUID.randomUUID()), DomainId.of(UUID.randomUUID())]
      def originalPools = [Pool.of(Mock(ConnectionFactoryEntry), domainIds)]
      def poolDomainId = DomainId.of(UUID.randomUUID())
      def newPools =  [Pool.of(Mock(ConnectionFactoryEntry), [poolDomainId])]
      ConnectionFactoryEntryStore connectionFactoryEntryStore = Mock(ConnectionFactoryEntryStore)

      PoolManager instance = new PoolManager(Mock(Event), Mock(Event))
      when:
      def pools = instance.getPools()
      then:
      pools == originalPools
      when:
      instance.newConnection(poolDomainId)
      def result = instance.getPools()
      then:
      result != pools
      result == newPools
   }
}
