package se.kodarkatten.casual.network.messages.request

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.messages.request.domain.CasualDomainDiscoveryRequestMessage
import spock.lang.Specification

/**
 * Created by aleph on 2017-03-02.
 */
class CasualDomainDiscoveryRequestMessageTest extends Specification
{
    def "Roundtrip with message payload less than Integer.MAX_VALUE - one service and no queues"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['Very nice service']
        def queueNames = []
        def msg = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(execution)
                .setDomainId(domainId)
                .setDomainName(domainName)
                .setServiceNames(serviceNames)
                .build()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualDomainDiscoveryRequestMessage resurrectedMsg = CasualNetworkReader.networkDomainDiscoveryRequestToCasualDomainDiscoveryRequestMessage(networkBytes)

        then:
        networkBytes != null
        networkBytes.size() == 1
        msg == resurrectedMsg
        domainName == resurrectedMsg.getDomainName()
        serviceNames.size() == resurrectedMsg.getNumberOfRequestedServicesToFollow()
        queueNames.size() == resurrectedMsg.getNumberOfRequestedQueuesToFollow()
        serviceNames == resurrectedMsg.getServiceNames()
        queueNames == resurrectedMsg.getQueueNames()
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - no services and one queues"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = []
        def queueNames = ['The funny queue']
        def msg = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(execution)
                .setDomainId(domainId)
                .setDomainName(domainName)
                .setQueueNames(queueNames)
                .build()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualDomainDiscoveryRequestMessage resurrectedMsg = CasualNetworkReader.networkDomainDiscoveryRequestToCasualDomainDiscoveryRequestMessage(networkBytes)

        then:
        networkBytes != null
        networkBytes.size() == 1
        msg == resurrectedMsg
        domainName == resurrectedMsg.getDomainName()
        serviceNames.size() == resurrectedMsg.getNumberOfRequestedServicesToFollow()
        queueNames.size() == resurrectedMsg.getNumberOfRequestedQueuesToFollow()
        serviceNames == resurrectedMsg.getServiceNames()
        queueNames == resurrectedMsg.getQueueNames()
    }


    def "Roundtrip with message payload less than Integer.MAX_VALUE"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['Very nice service', 'Hola!']
        def queueNames = ['Queues of the world unite!']
        def msg = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                     .setExecution(execution)
                                                     .setDomainId(domainId)
                                                     .setDomainName(domainName)
                                                     .setServiceNames(serviceNames)
                                                     .setQueueNames(queueNames)
                                                     .build()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualDomainDiscoveryRequestMessage resurrectedMsg = CasualNetworkReader.networkDomainDiscoveryRequestToCasualDomainDiscoveryRequestMessage(networkBytes)

        then:
        networkBytes != null
        networkBytes.size() == 1
        msg == resurrectedMsg
        domainName == resurrectedMsg.getDomainName()
        serviceNames.size() == resurrectedMsg.getNumberOfRequestedServicesToFollow()
        queueNames.size() == resurrectedMsg.getNumberOfRequestedQueuesToFollow()
        serviceNames == resurrectedMsg.getServiceNames()
        queueNames == resurrectedMsg.getQueueNames()
    }

    def "Roundtrip forced to chunk - one service and no queues"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['Very nice service']
        def queueNames = []
        def msg = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(execution)
                .setDomainId(domainId)
                .setDomainName(domainName)
                .setServiceNames(serviceNames)
                .setMaxMessageSize(1)
                .build()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualDomainDiscoveryRequestMessage resurrectedMsg = CasualNetworkReader.networkDomainDiscoveryRequestToCasualDomainDiscoveryRequestMessage(networkBytes)

        then:
        networkBytes != null
        msg == resurrectedMsg
        domainName == resurrectedMsg.getDomainName()
        serviceNames.size() == resurrectedMsg.getNumberOfRequestedServicesToFollow()
        queueNames.size() == resurrectedMsg.getNumberOfRequestedQueuesToFollow()
        serviceNames == resurrectedMsg.getServiceNames()
        queueNames == resurrectedMsg.getQueueNames()
    }

    def "Roundtrip forced to chunk - no service and one queues"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = []
        def queueNames = ['Spiffy queue!']
        def msg = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(execution)
                .setDomainId(domainId)
                .setDomainName(domainName)
                .setQueueNames(queueNames)
                .setMaxMessageSize(1)
                .build()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualDomainDiscoveryRequestMessage resurrectedMsg = CasualNetworkReader.networkDomainDiscoveryRequestToCasualDomainDiscoveryRequestMessage(networkBytes)

        then:
        networkBytes != null
        msg == resurrectedMsg
        domainName == resurrectedMsg.getDomainName()
        serviceNames.size() == resurrectedMsg.getNumberOfRequestedServicesToFollow()
        queueNames.size() == resurrectedMsg.getNumberOfRequestedQueuesToFollow()
        serviceNames == resurrectedMsg.getServiceNames()
        queueNames == resurrectedMsg.getQueueNames()
    }

    def "Roundtrip with message payload required to be chunked"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['Very nice service', 'Another nice service']
        def queueNames = ['Queues of the world unite!']
        def msg = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(execution)
                .setDomainId(domainId)
                .setDomainName(domainName)
                .setServiceNames(serviceNames)
                .setQueueNames(queueNames)
                // only ever used in test
                .setMaxMessageSize(1)
                .build()
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualDomainDiscoveryRequestMessage resurrectedMsg = CasualNetworkReader.networkDomainDiscoveryRequestToCasualDomainDiscoveryRequestMessage(networkBytes)

        then:
        networkBytes != null
        msg == resurrectedMsg
        domainName == resurrectedMsg.getDomainName()
        serviceNames.size() == resurrectedMsg.getNumberOfRequestedServicesToFollow()
        queueNames.size() == resurrectedMsg.getNumberOfRequestedQueuesToFollow()
        serviceNames == resurrectedMsg.getServiceNames()
        queueNames == resurrectedMsg.getQueueNames()
    }

}
