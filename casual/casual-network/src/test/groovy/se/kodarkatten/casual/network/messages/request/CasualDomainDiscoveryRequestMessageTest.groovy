package se.kodarkatten.casual.network.messages.request

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.request.domain.CasualDomainDiscoveryRequestMessage
import se.kodarkatten.casual.network.utils.ByteSink
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
        def requestMessage = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(execution)
                .setDomainId(domainId)
                .setDomainName(domainName)
                .setServiceNames(serviceNames)
                .build()
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMessage)
        def sink = new ByteSink()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2 // header + msg
        msg == resurrectedMsg
        domainName == resurrectedMsg.getMessage().getDomainName()
        serviceNames.size() == resurrectedMsg.getMessage().getNumberOfRequestedServicesToFollow()
        queueNames.size() == resurrectedMsg.getMessage().getNumberOfRequestedQueuesToFollow()
        serviceNames == resurrectedMsg.getMessage().getServiceNames()
        queueNames == resurrectedMsg.getMessage().getQueueNames()
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - no services and one queues"()
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
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMessage)
        def sink = new ByteSink()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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


    def "Roundtrip with message payload less than Integer.MAX_VALUE"()
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
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMessage)
        def sink = new ByteSink()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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

    def "Roundtrip forced to chunk - one service and no queues"()
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
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMessage)
        def sink = new ByteSink()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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

    def "Roundtrip forced to chunk - no service and one queues"()
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
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMessage)
        def sink = new ByteSink()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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

    def "Roundtrip with message payload required to be chunked"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['Very nice service', 'Another nice service']
        def queueNames = ['Queues of the world unite!']
        def requestMessage = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(execution)
                .setDomainId(domainId)
                .setDomainName(domainName)
                .setServiceNames(serviceNames)
                .setQueueNames(queueNames)
                // only ever used in test
                .setMaxMessageSize(1)
                .build()
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMessage)
        def sink = new ByteSink()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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
