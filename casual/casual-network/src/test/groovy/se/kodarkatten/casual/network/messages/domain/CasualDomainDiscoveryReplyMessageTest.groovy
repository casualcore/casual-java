package se.kodarkatten.casual.network.messages.domain

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryReplyMessage
import se.kodarkatten.casual.network.messages.domain.Queue
import se.kodarkatten.casual.network.messages.domain.Service
import se.kodarkatten.casual.network.messages.domain.TransactionType
import se.kodarkatten.casual.network.utils.LocalByteChannel
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

    def "Roundtrip with message payload less than Integer.MAX_VALUE. No services and no queues."()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def replyMessage = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2 // header + msg
        msg.getMessage() == replyMessage
        msg == resurrectedMsg
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE. One service, no queues."()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def serviceNames = ['First service']
        def services = createSomeServices(serviceNames)
        def replyMessage = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                                                            .setServices(services)
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        msg.getMessage() == replyMessage
        msg == resurrectedMsg
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE. No services, one queue."()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def queueNames = ['A queue']
        def queues = createSomeQueues(queueNames)
        def replyMessage = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                                                            .setQueues(queues)
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        msg.getMessage() == replyMessage
        msg == resurrectedMsg
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE"()
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
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        msg.getMessage() == replyMessage
        msg == resurrectedMsg
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - forcing chunk"()
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
                                                            .setMaxMessageSize(1)
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), replyMessage)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        // force chunking when reading
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualDomainDiscoveryReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        then:
        networkBytes != null
        networkBytes.size() > 2
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
