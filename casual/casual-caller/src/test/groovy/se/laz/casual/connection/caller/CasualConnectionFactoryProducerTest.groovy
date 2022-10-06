/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller

import se.laz.casual.connection.caller.entities.ConnectionFactoryProducer
import spock.lang.Specification

class CasualConnectionFactoryProducerTest extends Specification
{
   def 'failed construction'()
   {
      when:
      ConnectionFactoryProducer.of(null)
      then:
      thrown(NullPointerException)
   }

   def 'ok construction'()
   {
      given:
      def jndiName = 'foo'
      when:
      def producer = ConnectionFactoryProducer.of(jndiName)
      then:
      producer.getJndiName() == jndiName
   }
}
