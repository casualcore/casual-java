/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller

import se.laz.casual.api.queue.QueueDetails
import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.api.service.ServiceDetails
import se.laz.casual.api.service.ServiceInfo
import se.laz.casual.connection.caller.entities.CacheEntry
import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry
import se.laz.casual.connection.caller.entities.MatchingEntry
import se.laz.casual.connection.caller.entities.Pool
import se.laz.casual.connection.caller.events.DomainGoneEvent
import se.laz.casual.connection.caller.events.NewDomainEvent
import se.laz.casual.jca.DomainId
import se.laz.casual.network.messages.domain.TransactionType
import spock.lang.Specification

import java.util.stream.Collectors

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
      def domainGone = Mock(DomainGoneEvent){
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
      domainGone = Mock(DomainGoneEvent){
         getDomainId() >> domainIdOne
      }
      instance.onDomainGone(domainGone)
      domainGone = Mock(DomainGoneEvent){
         getDomainId() >> domainIdTwo
      }
      instance.onDomainGone(domainGone)
      matches = instance.get(ServiceInfo.of(serviceName))
      then:
      matches.isEmpty()
   }

   def 'queue storage/retrieval/domain gone'()
   {
      given:
      def queueName = 'exampleQueue'
      def otherQueueName = 'otherQueue'
      def instance = new CacheImpl(Mock(PoolMatcher))
      def domainIdOne = DomainId.of(UUID.randomUUID())
      def connectionFactoryEntryOne = Mock(ConnectionFactoryEntry)
      QueueDetails queueDetailsOne = QueueDetails.of(queueName, 0)
      QueueDetails queueDetailsTwo = QueueDetails.of(queueName, 0)
      QueueDetails queueDetailsOtherQueueName = QueueDetails.of(otherQueueName, 0)
      def firstMatchingEntries = [MatchingEntry.of(connectionFactoryEntryOne, domainIdOne, Collections.emptyList(), [queueDetailsOne, queueDetailsTwo])]
      when:
      instance.store(firstMatchingEntries)
      def matches = instance.get(QueueInfo.of(queueName)).orElse(null)
      def expectedMatches = CacheEntry.of(domainIdOne, connectionFactoryEntryOne)
      then:
      matches == expectedMatches
      when: // add matching entries for some other queue - should have no impact on previous matching
      def yetAnotherMatchingEntries = [MatchingEntry.of(connectionFactoryEntryOne, domainIdOne, Collections.emptyList(), [queueDetailsOtherQueueName])]
      instance.store(yetAnotherMatchingEntries)
      matches = instance.get(QueueInfo.of(queueName)).orElse(null)
      then:
      matches == expectedMatches
      when: //on domain gone
      def domainGone = Mock(DomainGoneEvent){
         getDomainId() >> domainIdOne
      }
      instance.onDomainGone(domainGone)
      expectedMatches = null
      matches = instance.get(QueueInfo.of(queueName)).orElse(null)
      then:
      matches == expectedMatches
   }

   def 'services/queues available, a new domain connects'()
   {
      given:
      DomainId domainIdOne = DomainId.of(UUID.randomUUID())
      DomainId domainIdTwo = DomainId.of(UUID.randomUUID())
      DomainId domainIdThree = DomainId.of(UUID.randomUUID())

      def serviceName = 'exampleService'
      def otherServiceName = 'otherService'
      def yetAnotherServiceName = 'yetAnotherServiceName'

      def queueName = 'exampleQueue'
      def otherQueueName = 'otherQueue'
      def yetAnotherQueueName = 'yetAnotherQueueName'

      def connectionFactoryEntryOne = Mock(ConnectionFactoryEntry)
      def connectionFactoryEntryTwo = Mock(ConnectionFactoryEntry)
      def connectionFactoryEntryThree = Mock(ConnectionFactoryEntry)

      def newPool = Pool.of(connectionFactoryEntryThree, [domainIdThree])
      // match all entries
      def poolMatcher = Mock(PoolMatcher) {
         match(_, _, _) >> { List<ServiceInfo> services, List<QueueInfo> queues, List<Pool> pools ->
            def matchingEntries = []
            def matchingServices = services.stream()
                    .map({item -> createServiceDetails(item.getServiceName(), 99)})
                    .collect(Collectors.toList())
            def matchingQueues = queues.stream()
                    .map({item -> QueueDetails.of(item.getQueueName(),0)})
                    .collect(Collectors.toList())
            for(Pool pool : pools)
            {
               for(DomainId domainId : pool.getDomainIds())
               {
                  matchingEntries.add(createMatchingEntry(domainId, pool.getConnectionFactoryEntry(), matchingServices, matchingQueues))
               }
            }
            return matchingEntries
         }
      }
      def instance = new CacheImpl(poolMatcher)
      // First store some queues/services, it would normally occur during normal service/queue calls
      List<MatchingEntry> matchingEntries = [createMatchingEntry(domainIdOne, connectionFactoryEntryOne,[createServiceDetails(serviceName, 0)],[QueueDetails.of(queueName,0)]),
                                             createMatchingEntry(domainIdTwo, connectionFactoryEntryOne,[createServiceDetails(otherServiceName, 0)],[QueueDetails.of(otherQueueName,0)]),
                                             createMatchingEntry(domainIdOne, connectionFactoryEntryTwo,[createServiceDetails(yetAnotherServiceName, 0)],[QueueDetails.of(yetAnotherQueueName,0)])]
      instance.store(matchingEntries)
      def newDomainEvent = new NewDomainEvent(newPool)

      when:
      def matches = instance.get(ServiceInfo.of(serviceName))
      def expectedMatches = [CacheEntry.of(domainIdOne, connectionFactoryEntryOne)]
      then:
      matches == expectedMatches
      when:
      instance.onNewDomain(newDomainEvent)
      matches = instance.get(ServiceInfo.of(serviceName))
      expectedMatches = [CacheEntry.of(domainIdOne, connectionFactoryEntryOne), CacheEntry.of(domainIdThree, connectionFactoryEntryThree)]
      then: // first match should now also be available via the new domain
      matches == expectedMatches
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

   MatchingEntry createMatchingEntry(DomainId domainId, ConnectionFactoryEntry connectionFactoryEntry, ArrayList<ServiceDetails> serviceDetails, ArrayList<QueueDetails> queueDetails)
   {
      return MatchingEntry.of(connectionFactoryEntry, domainId, serviceDetails, queueDetails)
   }
}
