package se.laz.casual.jca

import spock.lang.Specification

class DomainIdReferenceCountedTest extends Specification
{
   def 'counting works as expected'()
   {
      given:
      DomainIdReferenceCounted instance = DomainIdReferenceCounted.of(DomainId.of(UUID.randomUUID()))
      when:
      def value = instance.incrementAndGet()
      then:
      value == 1
      when:
      value = instance.decrementAndGet()
      then:
      value == 0
   }

   def 'value equality'()
   {
      given:
      DomainId domainId = DomainId.of(UUID.randomUUID())
      DomainIdReferenceCounted first
      DomainIdReferenceCounted second
      when:
      first = DomainIdReferenceCounted.of(domainId)
      second = DomainIdReferenceCounted.of(domainId)
      then:
      first == second
      when:
      first.incrementAndGet()
      then:
      first != second
   }

}
