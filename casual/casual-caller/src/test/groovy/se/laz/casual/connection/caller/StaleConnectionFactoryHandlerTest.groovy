/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller

import se.laz.casual.jca.CasualConnectionFactory
import spock.lang.Specification

import javax.naming.InitialContext

class StaleConnectionFactoryHandlerTest extends Specification
{
   def 'no stale entries'()
   {
      given:
      def entries = [createEntry(false), createEntry(false), createEntry(false), createEntry(false),createEntry(false)]
      when:
      def newEntries = StaleConnectionFactoryHandler.of().revalidateConnectionFactories(entries, Mock(InitialContext))
      then:
      newEntries == entries
   }

   def 'stale entry'()
   {
      given:
      def entries = [createEntry(false), createEntry(false), createEntry(true,'stale'), createEntry(false),createEntry(false)]
      def context = Mock(InitialContext)
      def newConnectionFactory = Mock(CasualConnectionFactory)
      context.lookup(_) >> newConnectionFactory
      when:
      def newEntries = StaleConnectionFactoryHandler.of().revalidateConnectionFactories(entries, context)
      def replacedEntry = newEntries.stream()
                                                       .filter({it.getJndiName() == 'stale'})
                                                       .findFirst()
                                                       .orElseThrow( {new RuntimeException("stale entry not found")})
      then:
      newEntries != entries
      newEntries.size() == entries.size()
      replacedEntry.connectionFactory == newConnectionFactory
   }

   def createEntry(boolean stale)
   {
      return createEntry(stale, UUID.randomUUID().toString())
   }

   def createEntry(boolean stale, String jndiName)
   {
      def entry = Mock(ConnectionFactoryEntry)
      entry.isInvalid() >> stale
      entry.getJndiName() >> jndiName
      return entry
   }

}
