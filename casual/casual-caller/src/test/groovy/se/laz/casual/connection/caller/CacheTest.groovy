/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller

import se.laz.casual.api.discovery.DiscoveryReturn
import se.laz.casual.api.queue.QueueDetails
import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.api.service.ServiceDetails
import se.laz.casual.jca.CasualConnectionFactory
import se.laz.casual.network.messages.domain.TransactionType
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Collectors

class CacheTest extends Specification
{
   @Shared
   Cache instance
   @Shared
   def connectionFactoryOne = Mock(CasualConnectionFactory)
   @Shared
   def jndiNameOne = 'eis/CasualConnectionFactory'
   @Shared
   def connectionFactoryTwo = Mock(CasualConnectionFactory)
   @Shared
   def jndiNameTwo = 'eis/AnotherCasualConnectionFactory'
   @Shared
   ConnectionFactoryProducer producerOne = {
      def mock = Mock(ConnectionFactoryProducer)
      mock.getConnectionFactory() >> {
         connectionFactoryOne
      }
      mock.getJndiName() >> {
         jndiNameOne
      }
      return mock
   }()
   @Shared
   ConnectionFactoryProducer producerTwo = {
      def mock = Mock(ConnectionFactoryProducer)
      mock.getConnectionFactory() >> {
         connectionFactoryTwo
      }
      mock.getJndiName() >> {
         jndiNameTwo
      }
      return mock
   }()
   @Shared
   def cacheEntryOne = ConnectionFactoryEntry.of(producerOne)
   @Shared
   def cacheEntryTwo = ConnectionFactoryEntry.of(producerTwo)
   @Shared
   def serviceName = 'casual.test.echo'
   @Shared
   def serviceNameOnlyFromConnectionFactoryOne = 'flash.gordon'
   @Shared
   def queueNameOnlyFromConnectionFactoryOne = 'nifty.queue'
   @Shared
   def qInfo = QueueInfo.of('space1.agrajag')
   @Shared
   def qInfoList = [QueueInfo.of('hairy.otter'), QueueInfo.of('drunken.monkey')]
   @Shared
   def serviceNames = ['casual.rollback', 'casually.casual']
   @Shared
   def priority = 17L
   @Shared
   def lowerPriority = priority - 1
   @Shared
   def priorityMapping
   @Shared
   def allServiceNames = ([serviceName, serviceNameOnlyFromConnectionFactoryOne] + serviceNames).stream()
                                                                                                 .distinct()
                                                                                                 .sorted()
                                                                                                 .collect(Collectors.toList())
   @Shared
   def allQueueNames = qInfoList.stream()
                                .map({v -> v.getQueueName()})
                                .distinct()
                                .sorted()
                                .collect(Collectors.toList())

   def setup()
   {
      instance = new Cache()
      qInfoList.forEach({ q -> instance.store(q, [cacheEntryOne, cacheEntryTwo]) })
      serviceNames.forEach({ s -> instance.store(s, ConnectionFactoriesByPriority.of([(priority): [cacheEntryTwo]])) })
      instance.store(serviceName, ConnectionFactoriesByPriority.of([(priority): [cacheEntryOne, cacheEntryTwo]]))
      instance.store(serviceNameOnlyFromConnectionFactoryOne, ConnectionFactoriesByPriority.of([(priority): [cacheEntryOne]]))
   }

   def 'store null cache entry'()
   {
      when:
      instance.store(serviceName, null)
      then:
      thrown(NullPointerException)
   }

   def 'set, get and remove service'()
   {
      given:
      def anotherServiceName = 'anotherServiceName'
      when:
      instance.store(anotherServiceName, ConnectionFactoriesByPriority.of([(priority): [cacheEntryOne, cacheEntryTwo]]))
      def entries = instance.get(anotherServiceName)
      then:
      entries.getForPriority(priority).size() == 2
      when:
      instance.removeService(anotherServiceName)
      entries = instance.get(anotherServiceName)
      then:
      entries.empty
   }

   def 'get missing service entry'()
   {
      when:
      def entries = instance.get('does-not-exist')
      then:
      entries.isEmpty()
   }

   def 'store queue null cache entry'()
   {
      when:
      instance.store(qInfo, null)
      then:
      thrown(NullPointerException)
   }

   def 'set and get queue'()
   {
      when:
      instance.store(qInfo, [cacheEntryOne])
      def entries = instance.getSingle(qInfo)
      then:
      entries.isPresent()
   }

   def 'get missing queue entry'()
   {
      given:
      def qinfoNotStored = QueueInfo.of("abc.Ford Prefect")
      when:
      def entries = instance.getSingle(qinfoNotStored)
      then:
      !entries.isPresent()
   }

   def 'get service with only one provider'()
   {
      when:
      def connectionFactoryByPriority = instance.get(serviceNameOnlyFromConnectionFactoryOne)
      then:
      connectionFactoryByPriority.getForPriority(priority).size() == 1
      connectionFactoryByPriority.getForPriority(priority).get(0) == cacheEntryOne
   }

   def 'getAll queues and services'()
   {
      when:
      def allEntries = instance.getAll()
      then:
      allEntries.get(CacheType.SERVICE).stream().distinct().sorted().collect(Collectors.toList()) == allServiceNames
      allEntries.get(CacheType.QUEUE).stream().distinct().sorted().collect(Collectors.toList()) == allQueueNames
   }


   def 'cache purge and repopulation via DiscoveryReturn - services'()
   {
      given:
      def discoveryReturn = Mock(DiscoveryReturn){
         getQueueDetails() >> {
            []
         }
         getServiceDetails() >> {
            [toServiceDetails(serviceNameOnlyFromConnectionFactoryOne), toServiceDetails(serviceName), serviceNames.stream()
                    .map({toServiceDetails(it)})
                    .collect(Collectors.toList())].flatten()
         }
      }
      when:
      instance.purge(cacheEntryOne)
      def afterPurge = instance.get(serviceNameOnlyFromConnectionFactoryOne)
      then:
      afterPurge.isEmpty()
      when:
      instance.repopulate(discoveryReturn, cacheEntryOne)
      def afterRepopulate = instance.get(serviceNameOnlyFromConnectionFactoryOne)
      then:
      afterRepopulate.getForPriority(priority).size() == 1
      afterRepopulate.getForPriority(priority).get(0) == cacheEntryOne
      when: //get entry services by 2 connections
      def moreThanOneConnection = instance.get(serviceName)
      then:
      moreThanOneConnection.getForPriority(priority).size() == 2
   }

   def 'cache purge and repopulation via DiscoveryReturn - queues'()
   {
      given:
      def discoveryReturn = Mock(DiscoveryReturn){
         getQueueDetails() >> {
            [qInfoList.stream()
                     .map({item -> toQueueDetails(item.getQueueName())})
                     .collect(Collectors.toList()),
             toQueueDetails(queueNameOnlyFromConnectionFactoryOne)].flatten()
         }
         getServiceDetails() >> {
            []
         }
      }
      println "queue details: ${discoveryReturn.getQueueDetails()}"
      println "first qname: ${qInfoList.get(0)}"
      def queueInfoOnlyConnectionOne = QueueInfo.of(queueNameOnlyFromConnectionFactoryOne)
      instance.store(queueInfoOnlyConnectionOne, [cacheEntryOne])
      when:
      def beforePurge = instance.get(queueInfoOnlyConnectionOne)
      then:
      beforePurge.size() == 1
      when: // get queue served from 2 places
      beforePurge = instance.get(qInfoList.get(0))
      then:
      beforePurge.size() == 2
      when:
      instance.purge(cacheEntryOne)
      def afterPurge = instance.get(queueInfoOnlyConnectionOne)
      then:
      afterPurge.isEmpty()
      when:
      afterPurge = instance.get(qInfoList.get(0))
      then:
      afterPurge.size() == 1
      when:
      instance.repopulate(discoveryReturn, cacheEntryOne)
      def afterRepopulate = instance.get(queueInfoOnlyConnectionOne)
      then:
      afterRepopulate.size() == 1
   }

   QueueDetails toQueueDetails(String name)
   {
      return QueueDetails.of(name, 0)
   }

   ServiceDetails toServiceDetails(name)
   {
      return ServiceDetails.createBuilder().withName(name)
              .withHops(priority)
              .withCategory('foo')
              .withTransactionType(TransactionType.AUTOMATIC)
              .build()
   }
}
