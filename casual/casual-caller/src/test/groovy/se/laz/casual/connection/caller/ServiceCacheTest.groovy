/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller

import se.laz.casual.jca.CasualConnectionFactory
import spock.lang.Specification

class ServiceCacheTest extends Specification
{
   def 'cached entry is replaced onEvent'()
   {
      given:
      def jndiName = 'foo'
      def serviceName = 'fnord'
      def priority = 1
      ServiceCache serviceCache = new ServiceCache()
      ConnectionFactoriesByPriority connectionFactoriesByPriority= new ConnectionFactoriesByPriority()
      ConnectionFactoryEntry originalEntry = ConnectionFactoryEntry.of(jndiName, Mock(CasualConnectionFactory))
      connectionFactoriesByPriority.store(priority, [originalEntry])
      serviceCache.store(serviceName, connectionFactoriesByPriority)
      CasualConnectionFactory newConnectionFactory = Mock(CasualConnectionFactory)
      ConnectionFactoryEntry newEntry = ConnectionFactoryEntry.of(jndiName, newConnectionFactory)
      ConnectionFactoryEntryChangedEvent changedEvent = new ConnectionFactoryEntryChangedEvent(jndiName, newEntry)
      when:
      serviceCache.onEvent(changedEvent)
      then:
      !serviceCache.getOrEmpty(serviceName).isEmpty()
      serviceCache.getOrEmpty(serviceName).randomizeWithPriority().size() == 1
      serviceCache.getOrEmpty(serviceName).randomizeWithPriority().get(0).getConnectionFactory() == newConnectionFactory
   }
}
