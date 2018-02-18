package se.kodarkatten.casual.network.protocol.messages.domain

import se.kodarkatten.casual.network.protocol.io.CasualNetworkReader
import se.kodarkatten.casual.network.protocol.io.CasualNetworkWriter
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel
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
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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

    def "Roundtrip with message payload required to be chunked -sync"()
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
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

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
