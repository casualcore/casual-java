/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound

import se.laz.casual.jca.ConnectionObserver
import se.laz.casual.jca.DomainId
import spock.lang.Specification

class ConnectionObserverHandlerTest extends Specification
{
   def 'each observer gets one topology change notification'()
   {
      given:
      DomainId domainId = DomainId.of(UUID.randomUUID())
      ConnectionObserverHandler instance = ConnectionObserverHandler.of()
      ConnectionObserver observerOne = Mock(ConnectionObserver)
      ConnectionObserver observerTwo = Mock(ConnectionObserver)
      instance.addConnectionObserver(observerOne)
      instance.addConnectionObserver(observerTwo)
      instance.addConnectionObserver(observerOne)

      when:
      instance.notifyTopologyChanged(domainId)

      then:
      1 * observerOne.topologyChanged(domainId)
      1 * observerTwo.topologyChanged(domainId)
   }

   def 'each observer gets one domain disconnect notification'()
   {
      given:
      DomainId domainId = DomainId.of(UUID.randomUUID())
      ConnectionObserverHandler instance = ConnectionObserverHandler.of()
      ConnectionObserver observerOne = Mock(ConnectionObserver)
      ConnectionObserver observerTwo = Mock(ConnectionObserver)
      instance.addConnectionObserver(observerOne)
      instance.addConnectionObserver(observerTwo)
      instance.addConnectionObserver(observerOne)

      when:
      instance.notifyDomainDisconnected(domainId)

      then:
      1 * observerOne.domainDisconnected(domainId)
      1 * observerTwo.domainDisconnected(domainId)
   }

   def 'a failing observer does not prevent remaining observers from being notified'()
   {
      given:
      DomainId domainId = DomainId.of(UUID.randomUUID())
      ConnectionObserverHandler instance = ConnectionObserverHandler.of()
      ConnectionObserver failingObserver = Mock(ConnectionObserver)
      ConnectionObserver healthyObserver = Mock(ConnectionObserver)
      instance.addConnectionObserver(failingObserver)
      instance.addConnectionObserver(healthyObserver)

      when:
      instance.notifyDomainDisconnected(domainId)

      then:
      1 * failingObserver.domainDisconnected(domainId) >> { throw new IllegalStateException('Observer failed') }
      1 * healthyObserver.domainDisconnected(domainId)
      noExceptionThrown()
   }
}
