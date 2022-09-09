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
      CasualConnectionListener listener = Mock(CasualConnectionListener){
         2 * newConnection(_)
         1 * connectionGone(_)
      }
      CasualConnectionListener anotherListener = Mock(CasualConnectionListener){
         0 * newConnection(_)
         0 * connectionGone(_)
      }
      Address anotherAddress = Address.of("fnord", 64738)
      DomainHandler instance = new DomainHandler()
      instance.addConnectionListener(address, listener)
      instance.addConnectionListener(anotherAddress, anotherListener)
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

   def 'Same domain id multiple times'()
   {
      given:
      Address address = Address.of("morpheus", 64738)
      DomainId domainId = DomainId.of(UUID.randomUUID())
      def gotNewConnection = false
      def gotConnectionGone = false
      CasualConnectionListener listener = Mock(CasualConnectionListener){
         1 * newConnection(_) >> {
            gotNewConnection = true
         }
         1 * connectionGone(_) >> {
            gotConnectionGone = true
         }
      }
      CasualConnectionListener anotherListener = Mock(CasualConnectionListener){
         0 * newConnection(_)
         0 * connectionGone(_)
      }
      Address anotherAddress = Address.of("fnord", 64738)
      DomainHandler instance = new DomainHandler()
      instance.addConnectionListener(address, listener)
      instance.addConnectionListener(anotherAddress, anotherListener)
      when:
      instance.addDomainId(address, domainId)
      then:
      gotNewConnection == true
      gotConnectionGone == false
      when:
      gotNewConnection = false
      instance.addDomainId(address, domainId)
      instance.addDomainId(address, domainId)
      instance.addDomainId(address, domainId)
      instance.addDomainId(address, domainId)
      then:
      gotNewConnection == false
      gotConnectionGone == false
      when:
      instance.domainDisconnect(address, domainId)
      instance.domainDisconnect(address, domainId)
      instance.domainDisconnect(address, domainId)
      instance.domainDisconnect(address, domainId)
      then:
      gotNewConnection == false
      gotConnectionGone == false
      instance.domainDisconnect(address, domainId)
      then:
      gotNewConnection == false
      gotConnectionGone == true
      when:
      def ids = instance.getDomainIds(address)
      then:
      ids.isEmpty()
   }

}
