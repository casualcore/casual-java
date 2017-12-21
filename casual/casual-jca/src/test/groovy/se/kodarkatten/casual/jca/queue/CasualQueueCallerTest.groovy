package se.kodarkatten.casual.jca.queue

import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.queue.MessageSelector
import se.kodarkatten.casual.api.queue.QueueInfo
import se.kodarkatten.casual.api.queue.QueueMessage
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.jca.CasualManagedConnection
import se.kodarkatten.casual.jca.NetworkConnection
import se.kodarkatten.casual.network.connection.CasualConnectionException
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
    @Shared CasualDomainDiscoveryRequestMessage expectedDiscoverRequest
    @Shared CasualEnqueueRequestMessage expectedEnqueueRequest
    @Shared CasualDequeueRequestMessage expectedDequeueRequest
    @Shared CasualNWMessage<CasualDomainDiscoveryReplyMessage> discoveryReply
    @Shared CasualNWMessage<CasualDomainDiscoveryReplyMessage> failedDiscoveryReply
    @Shared CasualNWMessage<CasualEnqueueReplyMessage> enqueueReply
    @Shared CasualNWMessage<CasualDequeueReplyMessage> dequeueReply
    @Shared CasualNWMessage<CasualDomainDiscoveryRequestMessage> actualDiscoveryRequest
    @Shared CasualNWMessage<CasualEnqueueRequestMessage> actualEnqueueRequest
    @Shared CasualNWMessage<CasualEnqueueRequestMessage> actualDequeueRequest
    @Shared def bigBaddaBoom = 'big badda boom'

    def setup()
    {
        networkConnection = Mock(NetworkConnection)
        connection = new CasualManagedConnection( null, null )
        connection.networkConnection =  networkConnection

        connection.getXAResource().start( XID.of(), 0 )

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
        expectedDiscoverRequest = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                     .setDomainId(domainId)
                                                                     .setExecution(executionId)
                                                                     .setDomainName( connection.getDomainName() )
                                                                     .setQueueNames(Arrays.asList(queueInfo.compositeName))
                                                                     .build()

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
    }

    def initialiseReplies()
    {
        discoveryReply = createDomainDiscoveryReplyMessage( queueName )
        failedDiscoveryReply = createDomainDiscoveryReplyMessage()
        enqueueReply = createEnqueueReplyMessage()
        dequeueReply = createDequeueReplyMessage()
    }



    CasualNWMessage<CasualDomainDiscoveryReplyMessage> createDomainDiscoveryReplyMessage(String... queues )
    {
        CasualDomainDiscoveryReplyMessage msg = CasualDomainDiscoveryReplyMessage.of(executionId, domainId, domainName )
        List<Queue> available = new ArrayList<>()
        for( String s:  queues)
        {
            available.add(Queue.of(queueInfo.compositeName))
        }
        msg.setQueues(available)
        return CasualNWMessage.of( executionId, msg )
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
            CasualNWMessage<CasualDomainDiscoveryRequestMessage> input ->
                actualDiscoveryRequest = input
                return discoveryReply
        }
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualEnqueueRequestMessage> input ->
                actualEnqueueRequest = input
                return enqueueReply
        }
        expect actualDiscoveryRequest, matching( expectedDiscoverRequest )
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
            CasualNWMessage<CasualDomainDiscoveryRequestMessage> input ->
                actualDiscoveryRequest = input
                return discoveryReply
        }
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualEnqueueRequestMessage> input ->
                throw new RuntimeException(bigBaddaBoom)
        }
        expect actualDiscoveryRequest, matching( expectedDiscoverRequest )
    }

    def 'dequeue goes big badda boom'()
    {
        when:
        List<QueueMessage> messages = instance.dequeue(queueInfo, MessageSelector.of())
        then:
        messages == null
        def e = thrown(RuntimeException)
        e.message == bigBaddaBoom
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualDomainDiscoveryRequestMessage> input ->
                actualDiscoveryRequest = input
                return discoveryReply
        }
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualDequeueRequestMessage> input ->
                throw new RuntimeException(bigBaddaBoom)
        }
        expect actualDiscoveryRequest, matching( expectedDiscoverRequest)
    }

    def 'domain discovery failure during enqueue request throws exception after discovery failure'()
    {
        when:
        UUID msgId = instance.enqueue(queueInfo, QueueMessage.of(message))
        then:
        null == msgId
        def e = thrown(CasualConnectionException)
        e.message == "queue ${queueInfo.compositeName} does not exist"
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualDomainDiscoveryRequestMessage> input ->
                actualDiscoveryRequest = input
                return failedDiscoveryReply
        }
        0 * networkConnection.requestReply( _ as CasualNWMessage<CasualEnqueueRequestMessage> )
    }

    def 'domain discovery failure during dequeue request throws exception after discovery failure'()
    {
        when:
        List<QueueMessage> messages = instance.dequeue(queueInfo, MessageSelector.of())
        then:
        null == messages
        def e = thrown(CasualConnectionException)
        e.message == "queue ${queueInfo.compositeName} does not exist"
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualDomainDiscoveryRequestMessage> input ->
                actualDiscoveryRequest = input
                return failedDiscoveryReply
        }
        0 * networkConnection.requestReply( _ as CasualNWMessage<CasualDequeueRequestMessage> )
    }


}
