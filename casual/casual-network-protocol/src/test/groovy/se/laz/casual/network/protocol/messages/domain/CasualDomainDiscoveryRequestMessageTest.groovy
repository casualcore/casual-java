/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain

import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.LocalByteChannel
import spock.lang.Specification

/**
 * Created by aleph on 2017-03-02.
 */
class CasualDomainDiscoveryRequestMessageTest extends Specification
{
    def "Roundtrip with message payload less than Integer.MAX_VALUE - no services and one queues - sync"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = []
        def queueNames = ['The funny queue']
        def requestMessage = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(execution)
                .setDomainId(domainId)
                .setDomainName(domainName)
                .setQueueNames(queueNames)
                .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        msg == resurrectedMsg
        domainName == resurrectedMsg.getMessage().getDomainName()
        serviceNames.size() == resurrectedMsg.getMessage().getNumberOfRequestedServicesToFollow()
        queueNames.size() == resurrectedMsg.getMessage().getNumberOfRequestedQueuesToFollow()
        serviceNames == resurrectedMsg.getMessage().getServiceNames()
        queueNames == resurrectedMsg.getMessage().getQueueNames()
    }


    def "Roundtrip with message payload less than Integer.MAX_VALUE - sync"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['Very nice service', 'Hola!']
        def queueNames = ['Queues of the world unite!']
        def requestMessage = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(execution)
                .setDomainId(domainId)
                .setDomainName(domainName)
                .setServiceNames(serviceNames)
                .setQueueNames(queueNames)
                .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        msg == resurrectedMsg
        domainName == resurrectedMsg.getMessage().getDomainName()
        serviceNames.size() == resurrectedMsg.getMessage().getNumberOfRequestedServicesToFollow()
        queueNames.size() == resurrectedMsg.getMessage().getNumberOfRequestedQueuesToFollow()
        serviceNames == resurrectedMsg.getMessage().getServiceNames()
        queueNames == resurrectedMsg.getMessage().getQueueNames()
    }

    def "Roundtrip forced to chunk - one service and no queues, sync"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['Very nice service']
        def queueNames = []
        def requestMessage = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(execution)
                .setDomainId(domainId)
                .setDomainName(domainName)
                .setServiceNames(serviceNames)
                .setMaxMessageSize(1)
                .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() > 2
        msg == resurrectedMsg
        domainName == resurrectedMsg.getMessage().getDomainName()
        serviceNames.size() == resurrectedMsg.getMessage().getNumberOfRequestedServicesToFollow()
        queueNames.size() == resurrectedMsg.getMessage().getNumberOfRequestedQueuesToFollow()
        serviceNames == resurrectedMsg.getMessage().getServiceNames()
        queueNames == resurrectedMsg.getMessage().getQueueNames()
    }

    def "Roundtrip forced to chunk - no service and one queues, sync"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = []
        def queueNames = ['Spiffy queue!']
        def requestMessage = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(execution)
                .setDomainId(domainId)
                .setDomainName(domainName)
                .setQueueNames(queueNames)
                .setMaxMessageSize(1)
                .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() > 2
        msg == resurrectedMsg
        domainName == resurrectedMsg.getMessage().getDomainName()
        serviceNames.size() == resurrectedMsg.getMessage().getNumberOfRequestedServicesToFollow()
        queueNames.size() == resurrectedMsg.getMessage().getNumberOfRequestedQueuesToFollow()
        serviceNames == resurrectedMsg.getMessage().getServiceNames()
        queueNames == resurrectedMsg.getMessage().getQueueNames()
    }

}
