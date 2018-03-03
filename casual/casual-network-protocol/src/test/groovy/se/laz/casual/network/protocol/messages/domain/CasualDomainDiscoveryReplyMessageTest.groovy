/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain

import se.laz.casual.network.messages.domain.TransactionType
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.LocalByteChannel
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by aleph on 2017-03-08.
 */
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

    def "Message creation"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['First service', 'Second service']
        def services = createSomeServices(serviceNames)
        def queueNames = ['A queue', 'Another, surprise, queue!']
        def queues = createSomeQueues(queueNames)
        when:
        def msg = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
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
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE. No services and no queues - sync"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def replyMessage = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2 // header + msg
        msg.getMessage() == replyMessage
        msg == resurrectedMsg
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE. One service, no queues - sync"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['First service']
        def services = createSomeServices(serviceNames)
        def replyMessage = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                .setServices(services)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        msg.getMessage() == replyMessage
        msg == resurrectedMsg
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE. No services, one queue - sync"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def queueNames = ['A queue']
        def queues = createSomeQueues(queueNames)
        def replyMessage = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                .setQueues(queues)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        msg.getMessage() == replyMessage
        msg == resurrectedMsg
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
        def queues = createSomeQueues(queueNames)
        def replyMessage = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                .setServices(services)
                .setQueues(queues)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        msg.getMessage() == replyMessage
        msg == resurrectedMsg
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

    def createSomeQueues(List<String> names)
    {
        def queues = []
        names.each{
            queues << Queue.of(it.toString())
                    .setRetries(retries)
        }
        return queues
    }
}
