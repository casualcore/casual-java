/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.discovery

import se.laz.casual.api.discovery.DiscoveryReturn
import se.laz.casual.api.queue.QueueDetails
import se.laz.casual.config.json.Domain
import se.laz.casual.info.CasualInfo
import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.jca.CasualManagedConnection
import se.laz.casual.jca.CasualManagedConnectionFactory
import se.laz.casual.jca.DomainId
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.messages.domain.TransactionType
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import se.laz.casual.network.protocol.messages.domain.Queue
import se.laz.casual.network.protocol.messages.domain.Service
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

import static se.laz.casual.test.matchers.CasualNWMessageMatchers.matching
import static spock.util.matcher.HamcrestSupport.expect

class CasualDiscoveryCallerTest extends Specification
{
    @Shared DomainId domainOne = DomainId.of(UUID.randomUUID())

    CasualDiscoveryCaller instance
    CasualManagedConnection connection
    CasualManagedConnectionFactory mcf
    NetworkConnection networkConnection
    UUID executionId
    UUID domainId
    String domainName
    UUID corrid

    List<String> serviceNames
    List<String> queueNames

    CasualDomainDiscoveryRequestMessage expectedDiscoveryRequest
    CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> actualDiscoveryRequest

    def setup()
    {
        mcf = Mock(CasualManagedConnectionFactory)
        networkConnection = Mock(NetworkConnection) {
            getDomainId() >> domainOne
            getProtocolVersion() >> ProtocolVersion.VERSION_1_2
        }
        connection = new CasualManagedConnection(mcf)
        connection.networkConnection = networkConnection

        instance = CasualDiscoveryCaller.of(connection)

        initialiseParameters()
        initialiseExpectedRequests()
    }

    def initialiseParameters()
    {
        executionId = UUID.randomUUID()
        domainId = UUID.randomUUID()
        domainName = Domain.getName()
        corrid = UUID.randomUUID()
        serviceNames = ['testService', 'anotherService']
        queueNames = ['testQueue', 'anotherQueue']
        mcf.getHostName(  ) >> "somehost"
        mcf.getPortNumber(  ) >> 8080
    }

    def initialiseExpectedRequests()
    {
        expectedDiscoveryRequest = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setServiceNames(serviceNames)
                .setQueueNames(queueNames)
                .setDomainName(Domain.getName())
                .build()
    }

    CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> createDiscoveryReply(
            List<Service> services, List<Queue> queues, ProtocolVersion protocolVersion)
    {
        CasualNWMessageImpl.of(executionId,
                CasualDomainDiscoveryReplyMessage.of(executionId, domainId, domainName, protocolVersion)
                        .setServices(services)
                        .setQueues(queues))
    }

    List<Service> asServices(List<Map> serviceData)
    {
        serviceData.collect {
            Service.of(it.name as String, it.category as String, it.transactionType as TransactionType)
                   .setTimeout(it.timeout as long)
                   .setHops(it.hops as long)
        }
    }

    List<Queue> asQueues(List<String> queueNames, ProtocolVersion protocolVersion)
    {
        queueNames.collect { Queue.createBuilder().withName(it).withProtocolVersion( protocolVersion).build() }
    }

    def 'of - null managed connection throws NullPointerException'()
    {
        when:
        CasualDiscoveryCaller.of(null)

        then:
        thrown(NullPointerException)
    }

    def 'discover - stores discovery information'()
    {
        setup:
        def protocolVersion = ProtocolVersion.VERSION_1_4
        def services = asServices([
                [name: 'testService', category: 'test', transactionType: TransactionType.AUTOMATIC, timeout: 5000L, hops: 1L],
                [name: 'anotherService', category: 'other', transactionType: TransactionType.JOIN, timeout: 3000L, hops: 2L]
        ])
        def queues = asQueues(['testQueue', 'anotherQueue'], protocolVersion)
        def reply = createDiscoveryReply(services, queues, protocolVersion)

        networkConnection = Mock(NetworkConnection) {
            getDomainId() >> domainOne
            getProtocolVersion() >> protocolVersion
        }
        connection.networkConnection = networkConnection

        when:
        instance.discover(corrid, serviceNames, queueNames)

        then:
        1 * networkConnection.request(_) >> {
            CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> input ->
                actualDiscoveryRequest = input
                return CompletableFuture.completedFuture(reply)
        }
        CasualInfo.getOutboundServices(  ).size(  ) == 2
    }

    def 'discover - returns services and queues from reply, protocol version #protocolVersion'()
    {
        setup:
        def services = asServices([
                [name: 'testService', category: 'test', transactionType: TransactionType.AUTOMATIC, timeout: 5000L, hops: 1L],
                [name: 'anotherService', category: 'other', transactionType: TransactionType.JOIN, timeout: 3000L, hops: 2L]
        ])
        def queues = asQueues(['testQueue', 'anotherQueue'], protocolVersion)
        def reply = createDiscoveryReply(services, queues, protocolVersion)

        networkConnection = Mock(NetworkConnection) {
            getDomainId() >> domainOne
            getProtocolVersion() >> protocolVersion
        }
        connection.networkConnection = networkConnection

        when:
        DiscoveryReturn result = instance.discover(corrid, serviceNames, queueNames)

        then:
        1 * networkConnection.request(_) >> {
            CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> input ->
                actualDiscoveryRequest = input
                return CompletableFuture.completedFuture(reply)
        }
        expect actualDiscoveryRequest, matching(expectedDiscoveryRequest)

        result != null
        result.getServiceDetails().size() == 2

        result.getServiceDetails()[0].getName() == 'testService'
        result.getServiceDetails()[0].getCategory() == 'test'
        result.getServiceDetails()[0].getTransactionType() == TransactionType.AUTOMATIC
        result.getServiceDetails()[0].getTimeout() == 5000L
        result.getServiceDetails()[0].getHops() == 1L

        result.getServiceDetails()[1].getName() == 'anotherService'
        result.getServiceDetails()[1].getCategory() == 'other'
        result.getServiceDetails()[1].getTransactionType() == TransactionType.JOIN
        result.getServiceDetails()[1].getTimeout() == 3000L
        result.getServiceDetails()[1].getHops() == 2L

        result.getQueueDetails().size() == 2
        result.getQueueDetails()[0].getName() == 'testQueue'
        result.getQueueDetails()[1].getName() == 'anotherQueue'

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_2, ProtocolVersion.VERSION_1_3]
    }

    def 'discover - protocol version 1.4 includes extended queue details'()
    {
        setup:
        def protocolVersion = ProtocolVersion.VERSION_1_4
        def services = asServices([
                [name: 'svc1', category: 'cat1', transactionType: TransactionType.AUTOMATIC, timeout: 1000L, hops: 0L]
        ])
        def queues = [
                Queue.createBuilder()
                        .withName('q1')
                        .withProtocolVersion(protocolVersion)
                        .withRetries(3L)
                        .withRetryDelay(500L)
                        .withEnqueueEnabled(true)
                        .withDequeueEnabled(false)
                        .build()
        ]
        def reply = createDiscoveryReply(services, queues, protocolVersion)

        networkConnection = Mock(NetworkConnection) {
            getDomainId() >> domainOne
            getProtocolVersion() >> protocolVersion
        }
        connection.networkConnection = networkConnection

        when:
        DiscoveryReturn result = instance.discover(corrid, serviceNames, queueNames)

        then:
        1 * networkConnection.request(_) >> {
            CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> input ->
                actualDiscoveryRequest = input
                return CompletableFuture.completedFuture(reply)
        }
        expect actualDiscoveryRequest, matching(expectedDiscoveryRequest)

        result != null

        result.getServiceDetails().size() == 1
        result.getServiceDetails()[0].getName() == 'svc1'

        result.getQueueDetails().size() == 1
        QueueDetails queueDetails = result.getQueueDetails()[0]
        queueDetails.getName() == 'q1'
        queueDetails.getRetries() == 3L
        queueDetails.getRetryDelay().isPresent()
        queueDetails.getRetryDelay().get() == 500L
        queueDetails.isEnqueueEnabled().isPresent()
        queueDetails.isEnqueueEnabled().get() == true
        queueDetails.isDequeueEnabled().isPresent()
        queueDetails.isDequeueEnabled().get() == false
    }

    def 'discover - pre 1.4 protocol does not include extended queue details'()
    {
        setup:
        def protocolVersion = ProtocolVersion.VERSION_1_3
        def queues = [Queue.createBuilder().withName('q1').withProtocolVersion(protocolVersion).withRetries(5L).build()]
        def reply = createDiscoveryReply([], queues, protocolVersion)

        networkConnection = Mock(NetworkConnection) {
            getDomainId() >> domainOne
            getProtocolVersion() >> protocolVersion
        }
        connection.networkConnection = networkConnection

        when:
        DiscoveryReturn result = instance.discover(corrid, serviceNames, queueNames)

        then:
        1 * networkConnection.request(_) >> {
           CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> input ->
              actualDiscoveryRequest = input
              return CompletableFuture.completedFuture(reply)
        }
        expect actualDiscoveryRequest, matching(expectedDiscoveryRequest)

        result != null
        result.getQueueDetails().size() == 1
        QueueDetails queueDetails = result.getQueueDetails()[0]
        queueDetails.getName() == 'q1'
        queueDetails.getRetries() == 5L
        queueDetails.getRetryDelay().isEmpty()
        queueDetails.isEnqueueEnabled().isEmpty()
        queueDetails.isDequeueEnabled().isEmpty()
    }

    def 'discover - empty services and queues in reply returns empty discovery return'()
    {
        setup:
        def reply = createDiscoveryReply([], [], ProtocolVersion.VERSION_1_2)

        when:
        DiscoveryReturn result = instance.discover(corrid, serviceNames, queueNames)

        then:
        1 * networkConnection.request(_) >> {
            CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> input ->
                actualDiscoveryRequest = input
                return CompletableFuture.completedFuture(reply)
        }
        expect actualDiscoveryRequest, matching(expectedDiscoveryRequest)

        result != null
        result.getServiceDetails().isEmpty()
        result.getQueueDetails().isEmpty()
    }

    def 'discover - only services, no queues'()
    {
        setup:
        def services = asServices([
                [name: 'svc1', category: 'cat1', transactionType: TransactionType.AUTOMATIC, timeout: 0L, hops: 0L]
        ])
        def expectedRequest = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setServiceNames([services.get(0).name])
                .setDomainName(Domain.getName())
                .build()
        def reply = createDiscoveryReply(services, [], ProtocolVersion.VERSION_1_2)

        when:
        DiscoveryReturn result = instance.discover(corrid, ['svc1'], [])

        then:
        1 * networkConnection.request(_) >> {
           CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> input ->
              actualDiscoveryRequest = input
              return CompletableFuture.completedFuture(reply)
        }
        expect actualDiscoveryRequest, matching(expectedRequest)

        result != null
        result.getServiceDetails().size() == 1
        result.getServiceDetails()[0].getName() == 'svc1'
        result.getQueueDetails().isEmpty()
    }

    def 'discover - only queues, no services'()
    {
        setup:
        def queues = asQueues(['q1'], ProtocolVersion.VERSION_1_2)
        def expectedRequest = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setQueueNames([queues.get(0).name])
                .setDomainName(Domain.getName())
                .build()
        def reply = createDiscoveryReply([], queues, ProtocolVersion.VERSION_1_2)

        when:
        DiscoveryReturn result = instance.discover(corrid, [], ['q1'])

        then:
        1 * networkConnection.request(_) >> {
           CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> input ->
              actualDiscoveryRequest = input
              return CompletableFuture.completedFuture(reply)
        }
        expect actualDiscoveryRequest, matching(expectedRequest)

        result != null
        result.getServiceDetails().isEmpty()
        result.getQueueDetails().size() == 1
        result.getQueueDetails()[0].getName() == 'q1'
    }

    def 'discover - network request fails, exception propagates'()
    {
       given:
       1 * networkConnection.request(_) >> {
          CompletableFuture f = new CompletableFuture<>()
          f.completeExceptionally(new RuntimeException('network failure'))
          return f
       }
       when:
       instance.discover(corrid, serviceNames, queueNames)
       then:
       thrown(Exception)
    }

    def 'discover - corrid is propagated to the network message'()
    {
        setup:
        def reply = createDiscoveryReply([], [], ProtocolVersion.VERSION_1_2)

        when:
        instance.discover(corrid, serviceNames, queueNames)

        then:
        1 * networkConnection.request(_) >> {
            CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> input ->
                assert input.getCorrelationId() == corrid
                return CompletableFuture.completedFuture(reply)
        }
    }
}
