package se.laz.casual.connection.caller

import se.laz.casual.api.service.ServiceDetails
import se.laz.casual.api.service.ServiceInfo
import se.laz.casual.connection.caller.entities.CacheEntry
import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry
import se.laz.casual.connection.caller.entities.MatchingEntry
import se.laz.casual.connection.caller.events.DomainGone
import se.laz.casual.jca.DomainId
import se.laz.casual.network.messages.domain.TransactionType
import spock.lang.Specification

class CacheImplTest extends Specification
{
   def 'service storage/retrieval/domain gone'()
   {
      given:
      def serviceName = 'exampleService'
      def otherServiceName = 'otherService'
      def yetAnotherServiceName = 'yetAnotherServiceName'
      def instance = new CacheImpl(Mock(PoolMatcher))
      def domainIdOne = DomainId.of(UUID.randomUUID())
      def domainIdTwo = DomainId.of(UUID.randomUUID())
      def connectionFactoryEntryOne = Mock(ConnectionFactoryEntry)
      def connectionFactoryEntryTwo = Mock(ConnectionFactoryEntry)
      def connectionFactoryEntryThree = Mock(ConnectionFactoryEntry)
      ServiceDetails serviceDetailsLowestHops = createServiceDetails(serviceName, 0)
      ServiceDetails serviceDetails2ndLowestHops = createServiceDetails(serviceName, 1)
      ServiceDetails serviceDetails3rdLowestHops = createServiceDetails(serviceName, 3)
      ServiceDetails otherServiceDetails = createServiceDetails(otherServiceName, 0)
      ServiceDetails yetAnotherServiceDetails = createServiceDetails(yetAnotherServiceName, 0)
      def firstMatchingEntries = [MatchingEntry.of(connectionFactoryEntryOne, domainIdOne, [serviceDetails3rdLowestHops, otherServiceDetails], Collections.emptyList())]
      when:
      instance.store(firstMatchingEntries)
      def matches = instance.get(ServiceInfo.of(serviceName))
      def expectedMatches = [CacheEntry.of(domainIdOne, connectionFactoryEntryOne)]
      then:
      connectionFactoryEntryOne != connectionFactoryEntryTwo
      connectionFactoryEntryThree != connectionFactoryEntryOne
      matches == expectedMatches
      when: // add matching entries for some other service - should have no impact on previous matching
      def yetAnotherMatchingEntries = [MatchingEntry.of(connectionFactoryEntryOne, domainIdOne, [yetAnotherServiceDetails], Collections.emptyList())]
      instance.store(yetAnotherMatchingEntries)
      matches = instance.get(ServiceInfo.of(serviceName))
      then:
      matches == expectedMatches
      when: // matching entry for same service, same connectionFactoryEntry but another domainId and with lower hops
      def anotherMatchingEntriesSameService = [MatchingEntry.of(connectionFactoryEntryOne, domainIdTwo, [serviceDetailsLowestHops], Collections.emptyList())]
      instance.store(anotherMatchingEntriesSameService)
      expectedMatches = [CacheEntry.of(domainIdTwo, connectionFactoryEntryOne), CacheEntry.of(domainIdOne, connectionFactoryEntryOne)]
      matches = instance.get(ServiceInfo.of(serviceName))
      then:
      matches == expectedMatches
      when: //one domain gone, second should remain
      def domainGone = Mock(DomainGone){
         getDomainId() >> domainIdTwo
      }
      instance.onDomainGone(domainGone)
      expectedMatches = [CacheEntry.of(domainIdOne, connectionFactoryEntryOne)]
      matches = instance.get(ServiceInfo.of(serviceName))
      then:
      matches == expectedMatches
      when: // matching entry for same service, same connectionFactoryEntry but another domainId and with lower hops
      anotherMatchingEntriesSameService = [MatchingEntry.of(connectionFactoryEntryOne, domainIdTwo, [serviceDetails2ndLowestHops], Collections.emptyList())]
      instance.store(anotherMatchingEntriesSameService)
      expectedMatches = [CacheEntry.of(domainIdTwo, connectionFactoryEntryOne), CacheEntry.of(domainIdOne, connectionFactoryEntryOne)]
      matches = instance.get(ServiceInfo.of(serviceName))
      then:
      matches == expectedMatches
      when: // both domains gone, nothing should remain
      domainGone = Mock(DomainGone){
         getDomainId() >> domainIdOne
      }
      instance.onDomainGone(domainGone)
      domainGone = Mock(DomainGone){
         getDomainId() >> domainIdTwo
      }
      instance.onDomainGone(domainGone)
      matches = instance.get(ServiceInfo.of(serviceName))
      then:
      matches.isEmpty()
   }

   ServiceDetails createServiceDetails(String serviceName, long hops)
   {
      return ServiceDetails.createBuilder()
                           .withName(serviceName)
                           .withHops(hops)
                           // note: These two does not matter at all in this context so we set them to whatever for test
                           .withCategory("")
                           .withTransactionType(TransactionType.AUTOMATIC)
                           .build()
   }
}
