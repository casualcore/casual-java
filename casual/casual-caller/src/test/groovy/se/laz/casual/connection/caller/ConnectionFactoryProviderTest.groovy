/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller

import se.laz.casual.connection.caller.util.ConnectionFactoryFinder
import spock.lang.Specification

class ConnectionFactoryProviderTest extends Specification
{
   def 'normal operation, everything is found during deployment - only 1 call to initialize ( 0 calls during test of get)'()
   {
      given:
      ConnectionFactoryEntry entry = Mock(ConnectionFactoryEntry)
      ConnectionFactoryFinder connectionFactoryFinder = Mock(ConnectionFactoryFinder)
      connectionFactoryFinder.findConnectionFactory(_) >>> [[entry]]
      // spying to verify the interaction
      ConnectionFactoryEntryStore instance = Spy(ConnectionFactoryEntryStore, constructorArgs: [connectionFactoryFinder]) {
         1 * initialize()
      }
      // @PostConstruct
      instance.initialize()
      when:
      List<ConnectionFactoryEntry> result = instance.get()
      then:
      result.size() == 1
   }

   def 'abnormal wls operation, nothing is found during deployment - 2 calls to initialize ( 1 calls during test of get)'()
   {
      given:
      ConnectionFactoryEntry entry = Mock(ConnectionFactoryEntry)
      ConnectionFactoryFinder connectionFactoryFinder = Mock(ConnectionFactoryFinder)
      connectionFactoryFinder.findConnectionFactory(_) >>> [[], [entry]]
      // spying to verify the interactions
      ConnectionFactoryEntryStore instance = Spy(ConnectionFactoryEntryStore, constructorArgs: [connectionFactoryFinder]) {
         2 * initialize()
      }
      // @PostConstruct
      instance.initialize()
      when:
      List<ConnectionFactoryEntry> result = instance.get()
      then:
      result.size() == 1
   }

}
