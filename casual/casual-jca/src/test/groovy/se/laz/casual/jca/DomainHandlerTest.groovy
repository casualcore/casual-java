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
      noExceptionThrown()
      when:
      instance.addDomainId(address, Mock(DomainId))
      then:
      noExceptionThrown()
   }

}
