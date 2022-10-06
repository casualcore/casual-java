/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.pool

import se.laz.casual.connection.caller.ConnectionFactoryEntryStore
import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry
import se.laz.casual.connection.caller.entities.ConnectionFactoryProducer
import se.laz.casual.jca.CasualConnection
import se.laz.casual.jca.CasualConnectionFactory
import se.laz.casual.jca.DomainId
import spock.lang.Specification

import javax.resource.spi.CommException

class PoolPollerTest extends Specification
{
   def 'updating with domain ids'()
   {
      given:
      DomainId domainId = DomainId.of(UUID.randomUUID())
      ConnectionFactoryProducer producer = Mock(ConnectionFactoryProducer){
         CasualConnection connection = Mock(CasualConnection){
            1 * getPoolDomainIds() >> [domainId]
         }
         CasualConnectionFactory conFac = Mock(CasualConnectionFactory){
            1 * getConnection() >> connection
         }
         1 * getConnectionFactory() >> conFac
      }
      ConnectionFactoryEntry connectionFactoryEntry = ConnectionFactoryEntry.of(producer)
      ConnectionFactoryEntryStore entryStore = Mock(ConnectionFactoryEntryStore){
         1  * get() >> {
            [connectionFactoryEntry]
         }
      }
      PoolManager poolManager = Mock(PoolManager){
         1 * updatePool(connectionFactoryEntry, [domainId])
      }
      PoolPoller instance = new PoolPoller()
      instance.connectionFactoryStore = entryStore
      instance.poolManager = poolManager
      when:
      instance.updatePools()
      then:
      noExceptionThrown()
   }

   def 'connection gone, updating with empty set'()
   {
      given:
      ConnectionFactoryProducer producer = Mock(ConnectionFactoryProducer){
         CasualConnectionFactory conFac = Mock(CasualConnectionFactory){
            1 * getConnection() >> {throw new CommException()}
         }
         1 * getConnectionFactory() >> conFac
      }
      ConnectionFactoryEntry connectionFactoryEntry = ConnectionFactoryEntry.of(producer)
      ConnectionFactoryEntryStore entryStore = Mock(ConnectionFactoryEntryStore){
         1  * get() >> {
            [connectionFactoryEntry]
         }
      }
      PoolManager poolManager = Mock(PoolManager){
         1 * updatePool(connectionFactoryEntry, [])
      }
      PoolPoller instance = new PoolPoller()
      instance.connectionFactoryStore = entryStore
      instance.poolManager = poolManager
      when:
      instance.updatePools()
      then:
      noExceptionThrown()
   }

}
