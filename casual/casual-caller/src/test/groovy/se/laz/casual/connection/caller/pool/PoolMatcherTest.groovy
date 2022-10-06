/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.pool

import se.laz.casual.api.discovery.DiscoveryReturn
import se.laz.casual.api.queue.QueueDetails
import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.api.service.ServiceDetails
import se.laz.casual.api.service.ServiceInfo
import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry
import se.laz.casual.connection.caller.entities.ConnectionFactoryProducer
import se.laz.casual.connection.caller.entities.Pool
import se.laz.casual.connection.caller.pool.PoolMatcher
import se.laz.casual.jca.CasualConnection
import se.laz.casual.jca.CasualConnectionFactory
import se.laz.casual.jca.CasualRequestInfo
import se.laz.casual.jca.DomainId
import se.laz.casual.network.messages.domain.TransactionType
import spock.lang.Specification

import javax.resource.spi.ConnectionRequestInfo
import javax.resource.spi.ResourceAdapterInternalException

class PoolMatcherTest extends Specification
{
   def 'matching queues and services'()
   {
      given:
      DomainId domainIdOne = DomainId.of(UUID.randomUUID())
      DomainId domainIdTwo = DomainId.of(UUID.randomUUID())

      ConnectionFactoryProducer connectionFactoryProducerOne = Mock(ConnectionFactoryProducer) {
         getConnectionFactory() >> {
            CasualConnectionFactory connectionFactory = Mock(CasualConnectionFactory) {
               getConnection(_) >> { ConnectionRequestInfo connectionRequestInfo ->
                  CasualRequestInfo requestInfo = (CasualRequestInfo) connectionRequestInfo
                  if (requestInfo.getDomainId().get() == domainIdOne) {
                     CasualConnection connection = Mock(CasualConnection) {
                        discover(_, _, _) >> { UUID corrid, List<String> serviceNames, List<String> queueNames ->
                           DiscoveryReturn.Builder builder = DiscoveryReturn.createBuilder()
                           for (String serviceName : serviceNames) {
                              builder.addServiceDetails(createServiceDetails(serviceName, 0))
                           }
                           for (String queueName : queueNames) {
                              builder.addQueueDetails(QueueDetails.of(queueName, 0))
                           }
                           return builder.build()
                        }
                     }
                     return connection
                  } else {
                     // would return null to appserver that would throw ResourceException this we throw here in test sans an appserver
                     throw new ResourceAdapterInternalException()
                  }
               }
            }
            return connectionFactory
         }
      }
      ConnectionFactoryEntry connectionFactoryEntryOne = ConnectionFactoryEntry.of(connectionFactoryProducerOne)
      Pool poolOne = Pool.of(connectionFactoryEntryOne, [domainIdOne, domainIdTwo])
      PoolMatcher poolMatcher = new PoolMatcher()
      def serviceName = "serviceName"
      def queueName = 'queueName'
      when:
      def matches = poolMatcher.match(ServiceInfo.of(serviceName), [poolOne])
      then:
      matches.size() == 1
      matches.get(0).getDomainId() == domainIdOne
      matches.get(0).getConnectionFactoryEntry() == connectionFactoryEntryOne
      matches.get(0).getServices().size() == 1
      matches.get(0).getQueues().isEmpty()
      matches.get(0).getServices().get(0).getName() == serviceName
      when:
      matches = poolMatcher.match(QueueInfo.of(queueName), [poolOne])
      then:
      matches.size() == 1
      matches.get(0).getDomainId() == domainIdOne
      matches.get(0).getConnectionFactoryEntry() == connectionFactoryEntryOne
      matches.get(0).getQueues().size() == 1
      matches.get(0).getServices().isEmpty()
      matches.get(0).getQueues().get(0).getName() == queueName
   }

   def 'no match'()
   {
      given:
      DomainId domainIdOne = DomainId.of(UUID.randomUUID())
      DomainId domainIdTwo = DomainId.of(UUID.randomUUID())

      ConnectionFactoryProducer connectionFactoryProducerOne = Mock(ConnectionFactoryProducer) {
         getConnectionFactory() >> {
            CasualConnectionFactory connectionFactory = Mock(CasualConnectionFactory) {
               getConnection(_) >> { ConnectionRequestInfo connectionRequestInfo ->
                  CasualRequestInfo requestInfo = (CasualRequestInfo) connectionRequestInfo
                  if (requestInfo.getDomainId().get() == domainIdOne) {
                     CasualConnection connection = Mock(CasualConnection) {
                        discover(_, _, _) >> { UUID corrid, List<String> serviceNames, List<String> queueNames ->
                           DiscoveryReturn.Builder builder = DiscoveryReturn.createBuilder()
                           return builder.build()
                        }
                     }
                     return connection
                     } else {
                     // would return null to appserver that would throw ResourceException this we throw here in test sans an appserver
                     throw new ResourceAdapterInternalException()
                  }
               }
            }
            return connectionFactory
         }
      }
      ConnectionFactoryEntry connectionFactoryEntryOne = ConnectionFactoryEntry.of(connectionFactoryProducerOne)
      Pool poolOne = Pool.of(connectionFactoryEntryOne, [domainIdOne, domainIdTwo])
      PoolMatcher poolMatcher = new PoolMatcher()
      def serviceName = "serviceName"
      def queueName = 'queueName'
      when:
      def matches = poolMatcher.match(ServiceInfo.of(serviceName), [poolOne])
      then:
      matches.isEmpty() == true
      when:
      matches = poolMatcher.match(QueueInfo.of(queueName), [poolOne])
      then:
      matches.isEmpty() == true
   }

   def 'match with empty queue and service info, should not result in a discovery call at all'()
   {
      DomainId domainIdOne = DomainId.of(UUID.randomUUID())
      DomainId domainIdTwo = DomainId.of(UUID.randomUUID())

      ConnectionFactoryProducer connectionFactoryProducerOne = Mock(ConnectionFactoryProducer) {
         getConnectionFactory() >> {
            CasualConnectionFactory connectionFactory = Mock(CasualConnectionFactory) {
               getConnection(_) >> { ConnectionRequestInfo connectionRequestInfo ->
                  CasualRequestInfo requestInfo = (CasualRequestInfo) connectionRequestInfo
                  if (requestInfo.getDomainId().get() == domainIdOne) {
                     CasualConnection connection = Mock(CasualConnection) {
                         0 * discover(_, _, _)
                     }
                     return connection
                  } else {
                     // would return null to appserver that would throw ResourceException this we throw here in test sans an appserver
                     throw new ResourceAdapterInternalException()
                  }
               }
            }
            return connectionFactory
         }
      }
      ConnectionFactoryEntry connectionFactoryEntryOne = ConnectionFactoryEntry.of(connectionFactoryProducerOne)
      PoolMatcher poolMatcher = new PoolMatcher()
      Pool poolOne = Pool.of(connectionFactoryEntryOne, [domainIdOne, domainIdTwo])
      when:
      def matches = poolMatcher.match([], [], [poolOne])
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
