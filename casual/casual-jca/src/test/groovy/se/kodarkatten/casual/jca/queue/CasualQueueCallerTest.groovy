package se.kodarkatten.casual.jca.queue

import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.queue.MessageSelector
import se.kodarkatten.casual.api.queue.QueueInfo
import se.kodarkatten.casual.api.queue.QueueMessage
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.jca.CasualManagedConnection
import se.kodarkatten.casual.jca.CasualManagedConnectionFactory
import se.kodarkatten.casual.jca.CasualResourceManager
import se.kodarkatten.casual.jca.NetworkConnection
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryReplyMessage
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage
import se.kodarkatten.casual.network.messages.domain.Queue
import se.kodarkatten.casual.network.messages.queue.*
import spock.lang.Shared
import spock.lang.Specification

import static se.kodarkatten.casual.jca.test.CasualNWMessageMatchers.matching
import static spock.util.matcher.HamcrestSupport.expect

class CasualQueueCallerTest extends Specification
{
    @Shared CasualQueueCaller instance
    @Shared CasualManagedConnection connection
    @Shared CasualManagedConnectionFactory mcf
    @Shared NetworkConnection networkConnection
    @Shared UUID executionId
    @Shared UUID domainId
    @Shared UUID enqueueReplyId
    @Shared def domainName
    @Shared def queueName
    @Shared def queueSpace
    @Shared QueueInfo queueInfo
    @Shared MessageSelector nullSelector = MessageSelector.of()
    @Shared JsonBuffer message
    @Shared CasualEnqueueRequestMessage expectedEnqueueRequest
    @Shared CasualDequeueRequestMessage expectedDequeueRequest
    @Shared CasualDomainDiscoveryRequestMessage expectedDomainDiscoveryRequest
    @Shared CasualNWMessage<CasualEnqueueReplyMessage> enqueueReply
    @Shared CasualNWMessage<CasualDequeueReplyMessage> dequeueReply
    @Shared CasualNWMessage<CasualDomainDiscoveryReplyMessage> domainDiscoveryReplyFound
    @Shared CasualNWMessage<CasualDomainDiscoveryReplyMessage> domainDiscoveryReplyNotFound
    @Shared CasualNWMessage<CasualEnqueueRequestMessage> actualEnqueueRequest
    @Shared CasualNWMessage<CasualDomainDiscoveryRequestMessage> actualDomainDiscoveryRequest
    @Shared def bigBaddaBoom = 'big badda boom'
    @Shared int resourceId = 42

    def setup()
    {
        mcf = Mock(CasualManagedConnectionFactory)
        mcf.getResourceId() >> {
            resourceId
        }
        networkConnection = Mock(NetworkConnection)
        connection = new CasualManagedConnection( mcf, null )
        connection.networkConnection =  networkConnection

        CasualResourceManager.getInstance().remove(XID.NULL_XID)
        connection.getXAResource().start( XID.NULL_XID, 0 )
        CasualResourceManager.getInstance().remove(XID.NULL_XID)

        instance = CasualQueueCaller.of( connection )

        initialiseParameters()
        initialiseExpectedRequests()
        initialiseReplies()
    }

    def initialiseParameters()
    {
        executionId = UUID.randomUUID()
        domainId = UUID.randomUUID()
        enqueueReplyId = UUID.randomUUID()
        domainName = connection.getDomainName()
        queueName = 'echo'
        queueSpace = 'asdf'
        queueInfo = QueueInfo.createBuilder().withQspace(queueSpace).withQname(queueName).build()
        message = JsonBuffer.of( "{msg: \"hello echo service.\"}" )
    }

    def initialiseExpectedRequests()
    {
        expectedEnqueueRequest = CasualEnqueueRequestMessage.createBuilder()
                                                            .withExecution(executionId)
                                                            .withXid(connection.getCurrentXid() )
                                                            .withQueueName(queueInfo.compositeName)
                                                            .withMessage(EnqueueMessage.of(QueueMessage.of(message)))
                                                            .build()

        expectedDequeueRequest = CasualDequeueRequestMessage.createBuilder()
                                                            .withXid(connection.getCurrentXid())
                                                            .withExecution(executionId)
                                                            .withSelectorUUID(nullSelector.getSelectorId())
                                                            .withSelectorProperties(nullSelector.getSelector())
                                                            .withQueueName(queueInfo.compositeName)
                                                            .withBlock(true)
                                                            .build()
        expectedDomainDiscoveryRequest = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                            .setQueueNames([queueInfo.compositeName])
                                                                            .setDomainName(connection.getDomainName())
                                                                            .build()
    }

    def initialiseReplies()
    {
        enqueueReply = createEnqueueReplyMessage()
        dequeueReply = createDequeueReplyMessage()
        domainDiscoveryReplyFound = createDomainDiscoveryReply(asQueues([queueInfo.compositeName]))
        domainDiscoveryReplyNotFound = createDomainDiscoveryReply(asQueues([]))
    }

    List<Queue> asQueues(List<String> queuenames)
    {
        List<Queue> l = new ArrayList<>()
        for(String qname : queuenames)
        {
            l.add(Queue.of(qname))
        }
        return l
    }

    CasualNWMessage<CasualDomainDiscoveryReplyMessage> createDomainDiscoveryReply(List<Queue> queues)
    {
        CasualNWMessage.of(executionId,
                           CasualDomainDiscoveryReplyMessage.of(executionId, domainId, domainName)
                                                            .setQueues(queues))
    }

    CasualNWMessage<CasualEnqueueReplyMessage> createEnqueueReplyMessage()
    {
        CasualNWMessage.of( executionId,
                CasualEnqueueReplyMessage.createBuilder()
                                         .withExecution(executionId)
                                         .withId(enqueueReplyId)
                                         .build())
    }

    CasualNWMessage<CasualDequeueReplyMessage> createDequeueReplyMessage()
    {
        CasualNWMessage.of(executionId,
                CasualDequeueReplyMessage.createBuilder()
                                         .withExecution(executionId)
                                         .withMessages(Arrays.asList(DequeueMessage.of(QueueMessage.of(message))))
                                         .build()
        )
    }

    def 'enqueue'()
    {
        when:
        UUID msgId = instance.enqueue(queueInfo, QueueMessage.of(message))
        then:
        noExceptionThrown()
        msgId == enqueueReplyId
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualEnqueueRequestMessage> input ->
                actualEnqueueRequest = input
                return enqueueReply
        }
        expect actualEnqueueRequest, matching( expectedEnqueueRequest )
    }

    def 'enqueue goes big badda boom'()
    {
        when:
        UUID msgId = instance.enqueue(queueInfo, QueueMessage.of(message))
        then:
        null == msgId
        def e = thrown(RuntimeException)
        e.message == bigBaddaBoom
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualEnqueueRequestMessage> input ->
                throw new RuntimeException(bigBaddaBoom)
        }
    }

    def 'dequeue goes big badda boom'()
    {
        when:
        List<QueueMessage> messages = instance.dequeue(queueInfo, MessageSelector.of())
        then:
        messages == null
        def e = thrown(RuntimeException)
        e.message == bigBaddaBoom
        1 * networkConnection.requestReply(_) >> {
            CasualNWMessage<CasualDequeueRequestMessage> input ->
                throw new RuntimeException(bigBaddaBoom)
        }
    }

    def 'queueExists'()
    {
        when:
        def r = instance.queueExists(queueInfo)
        then:
        r == true
        1 * networkConnection.requestReply(_) >> {
            CasualNWMessage<CasualDomainDiscoveryRequestMessage> input ->
                actualDomainDiscoveryRequest = input
                return domainDiscoveryReplyFound
        }
        expect actualDomainDiscoveryRequest, matching(expectedDomainDiscoveryRequest)
    }

    def 'queueExists - not found'()
    {
        when:
        def r = instance.queueExists(queueInfo)
        then:
        r == false
        1 * networkConnection.requestReply(_) >> {
            CasualNWMessage<CasualDomainDiscoveryRequestMessage> input ->
                actualDomainDiscoveryRequest = input
                return domainDiscoveryReplyNotFound
        }
        expect actualDomainDiscoveryRequest, matching(expectedDomainDiscoveryRequest)
    }

}
