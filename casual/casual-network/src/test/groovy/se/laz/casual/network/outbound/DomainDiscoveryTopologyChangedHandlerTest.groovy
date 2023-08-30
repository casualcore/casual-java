/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound

import se.laz.casual.jca.ConnectionObserver
import se.laz.casual.jca.DomainId
import spock.lang.Specification

class DomainDiscoveryTopologyChangedHandlerTest extends Specification
{
   def 'observers gets notified on topology change'()
   {
      given:
      DomainId domainId = DomainId.of(UUID.randomUUID())
      DomainDiscoveryTopologyChangedHandler instance = DomainDiscoveryTopologyChangedHandler.of()
      ConnectionObserver observerOne = Mock(ConnectionObserver){
         1 * topologyChanged(domainId)
      }
      ConnectionObserver observerTwo = Mock(ConnectionObserver){
         1 * topologyChanged(domainId)
      }
      instance.addConnectionObserver(observerOne)
      instance.addConnectionObserver (observerTwo)
      instance.addConnectionObserver(observerOne)
      instance.addConnectionObserver (observerTwo)
      instance.addConnectionObserver(observerOne)
      instance.addConnectionObserver (observerTwo)
      when:
      instance.notifyTopologyChanged(domainId)
      then:
      noExceptionThrown()
   }

}
