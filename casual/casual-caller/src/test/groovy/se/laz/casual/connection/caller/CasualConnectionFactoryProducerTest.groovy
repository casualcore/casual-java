package se.laz.casual.connection.caller


import spock.lang.Specification

class CasualConnectionFactoryProducerTest extends Specification
{
   def 'failed construction'()
   {
      when:
      CasualConnectionFactoryProducer.of(null)
      then:
      thrown(NullPointerException)
   }

   def 'ok construction'()
   {
      given:
      def jndiName = 'foo'
      when:
      def producer = CasualConnectionFactoryProducer.of(jndiName)
      then:
      producer.getJndiName() == jndiName
   }
}
