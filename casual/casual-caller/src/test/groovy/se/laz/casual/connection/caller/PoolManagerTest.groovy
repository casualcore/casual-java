/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller

import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry
import se.laz.casual.connection.caller.entities.Pool
import se.laz.casual.connection.caller.events.NewDomainEvent
import se.laz.casual.connection.caller.pool.PoolManager
import se.laz.casual.jca.DomainId
import spock.lang.Specification

import javax.enterprise.event.Event

class PoolManagerTest extends Specification
{
   def 'pool updating with new instances'()
   {
      given:
      def originalPools = []
      def newConnectionFactoryEntry = Mock(ConnectionFactoryEntry)
      def newDomainIds = [DomainId.of(UUID.randomUUID()), DomainId.of(UUID.randomUUID())]
      def newPools =  [Pool.of(newConnectionFactoryEntry, newDomainIds)]
      def newDomainEvent = Mock(Event){
         1 * fire(_)
      }
      def domainGoneEvent = Mock(Event){
         0 * fire(_)
      }
      PoolManager instance = new PoolManager(newDomainEvent, domainGoneEvent)
      when:
      def pools = instance.getPools()
      then:
      pools == originalPools
      when:
      instance.updatePool(newConnectionFactoryEntry, newDomainIds)
      def result = instance.getPools()
      then:
      result != pools
      result == newPools
   }
}
