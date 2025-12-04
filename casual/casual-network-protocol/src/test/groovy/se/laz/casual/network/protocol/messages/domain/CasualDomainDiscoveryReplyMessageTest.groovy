/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain

import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.messages.domain.TransactionType
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.LocalByteChannel
import spock.lang.Shared
import spock.lang.Specification

class CasualDomainDiscoveryReplyMessageTest extends Specification
{
    @Shared
    def timeout = 42
    @Shared
    def hops = 196
    @Shared
    def transactionType = TransactionType.AUTOMATIC
    @Shared
    def category = 'Very nifty category'
    @Shared
    def retries = 4
    @Shared
    def retryDelay = 5460
    @Shared
    def enqueueEnabled = true
    @Shared
    def dequeueEnabled = true

    def "Message creation"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['First service', 'Second service']
        def services = createSomeServices(serviceNames)
        def queueNames = ['A queue', 'Another, surprise, queue!']
        def queues = createSomeQueues(queueNames, protocolVersion)
        when:
        def msg = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName, ProtocolVersion.VERSION_1_3)
                                                   .setServices(services)
                                                   .setQueues(queues)
        then:
        msg != null
        msg.execution == execution
        msg.domainId == domainId
        msg.services.size() == services.size()
        msg.queues.size() == queues.size()
        msg.services == services
        msg.queues == queues
        where:
        protocolVersion << ProtocolVersion.values()
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE. No services and no queues - sync"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def replyMessage = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName, protocolVersion)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)

        then:
        networkBytes != null
        networkBytes.size() == 2 // header + msg
        msg.getMessage() == replyMessage
        msg == resurrectedMsg
        where:
        protocolVersion << ProtocolVersion.values()
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE. One service, no queues - sync"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['First service']
        def services = createSomeServices(serviceNames)
        def replyMessage = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName, protocolVersion)
                .setServices(services)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)

        then:
        networkBytes != null
        networkBytes.size() == 2
        msg.getMessage() == replyMessage
        msg == resurrectedMsg
        where:
        protocolVersion << ProtocolVersion.values()
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE. No services, one queue - sync"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def queueNames = ['A queue']
        def queues = createSomeQueues(queueNames, protocolVersion)
        def replyMessage = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName, protocolVersion)
                .setQueues(queues)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)

        then:
        networkBytes != null
        networkBytes.size() == 2
        msg.getMessage() == replyMessage
        msg == resurrectedMsg
        where:
        protocolVersion << ProtocolVersion.values()
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - sync"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['First service', 'Second service']
        def services = createSomeServices(serviceNames)
        def queueNames = ['A queue', 'Another, surprise, queue!']
        def queues = createSomeQueues(queueNames, protocolVersion)
        def replyMessage = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName, protocolVersion)
                .setServices(services)
                .setQueues(queues)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)

        then:
        networkBytes != null
        networkBytes.size() == 2
        msg.getMessage() == replyMessage
        msg == resurrectedMsg
        where:
        protocolVersion << ProtocolVersion.values()
    }

    def createSomeServices(List<String> names)
    {
        def services = []
        names.each{
            services << Service.of(it.toString(), category, transactionType)
                               .setTimeout(timeout)
                               .setHops(hops)
        }
        return services
    }

    def createSomeQueues(List<String> names, ProtocolVersion protocolVersion)
    {
        def queues = []
        names.each {
           if (protocolVersion.version >= ProtocolVersion.VERSION_1_4.version)
           {
              queues << Queue.of(it.toString(), protocolVersion)
                      .setRetries(retries)
                      .setRetryDelay(retryDelay)
                      .setEnqueueEnabled(enqueueEnabled)
                      .setDequeueEnabled(dequeueEnabled)
           }
           else
           {
              queues << Queue.of(it.toString(), protocolVersion)
                      .setRetries(retries)
           }
        }
        return queues
    }
}
