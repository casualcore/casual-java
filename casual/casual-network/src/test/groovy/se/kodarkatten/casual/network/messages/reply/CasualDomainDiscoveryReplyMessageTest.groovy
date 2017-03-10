package se.kodarkatten.casual.network.messages.reply

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.messages.queue.Queue
import se.kodarkatten.casual.network.messages.service.Service
import se.kodarkatten.casual.network.messages.service.TransactionType
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
        def msg = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualDomainDiscoveryReplyMessage resurrectedMsg = CasualNetworkReader.networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(networkBytes)

        then:
        networkBytes != null
        networkBytes.size() == 1
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
        def msg = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                                                   .setServices(services)

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualDomainDiscoveryReplyMessage resurrectedMsg = CasualNetworkReader.networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(networkBytes)

        then:
        networkBytes != null
        networkBytes.size() == 1
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
        def msg = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                                                   .setQueues(queues)

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualDomainDiscoveryReplyMessage resurrectedMsg = CasualNetworkReader.networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(networkBytes)

        then:
        networkBytes != null
        networkBytes.size() == 1
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
        def msg = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                                                   .setServices(services)
                                                   .setQueues(queues)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualDomainDiscoveryReplyMessage resurrectedMsg = CasualNetworkReader.networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(networkBytes)

        then:
        networkBytes != null
        networkBytes.size() == 1
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
        def msg = CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                                                   .setServices(services)
                                                   .setQueues(queues)
                                                   .setMaxMessageSize(1)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualDomainDiscoveryReplyMessage resurrectedMsg = CasualNetworkReader.networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(networkBytes)

        then:
        networkBytes != null
        networkBytes.size() > 1
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
