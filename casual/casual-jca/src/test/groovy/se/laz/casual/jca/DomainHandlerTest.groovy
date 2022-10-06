/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca


import spock.lang.Specification

class DomainHandlerTest extends Specification
{
   def 'domain connect and disconnect'()
   {
      given:
      Address address = Address.of("morpheus", 64738)
      DomainId domainId = DomainId.of(UUID.randomUUID())
      DomainHandler instance = new DomainHandler()
      when:
      instance.addDomainId(address, domainId)
      instance.domainDisconnect(address, domainId)
      then:
      instance.getDomainIds(address).isEmpty()
      when:
      instance.addDomainId(address, domainId)
      then:
      instance.getDomainIds(address).size() == 1
      instance.getDomainIds(address).contains(domainId)
      when: // same domain id again
      instance.addDomainId(address, domainId)
      then:
      instance.getDomainIds(address).size() == 1
      when: // one connection for the domain id goes away
      instance.domainDisconnect(address, domainId)
      then:
      instance.getDomainIds(address).size() == 1
      when: // the last connection for the domain id goes away
      instance.domainDisconnect(address, domainId)
      then:
      instance.getDomainIds(address).isEmpty()
   }


}
