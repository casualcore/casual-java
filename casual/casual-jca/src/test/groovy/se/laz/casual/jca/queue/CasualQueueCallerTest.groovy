/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.queue

import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.queue.DequeueReturn
import se.laz.casual.api.queue.EnqueueReturn
import se.laz.casual.api.queue.MessageSelector
import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.api.queue.QueueMessage
import se.laz.casual.api.xa.XID
import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.jca.CasualManagedConnection
import se.laz.casual.jca.CasualManagedConnectionFactory
import se.laz.casual.jca.CasualResourceManager
import se.laz.casual.config.Domain
import se.laz.casual.network.connection.CasualConnectionException
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import se.laz.casual.network.protocol.messages.domain.Queue
import se.laz.casual.network.protocol.messages.queue.*
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

import static se.laz.casual.test.matchers.CasualNWMessageMatchers.matching
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
    @Shared QueueInfo queueInfo
    @Shared MessageSelector nullSelector = MessageSelector.of()
    @Shared JsonBuffer message
    @Shared CasualEnqueueRequestMessage expectedEnqueueRequest
    @Shared CasualDequeueRequestMessage expectedDequeueRequest
    @Shared CasualDomainDiscoveryRequestMessage expectedDomainDiscoveryRequest
    @Shared CasualNWMessageImpl<CasualEnqueueReplyMessage> enqueueReply
    @Shared CasualNWMessageImpl<CasualDequeueReplyMessage> dequeueReply
    @Shared CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> domainDiscoveryReplyFound
    @Shared CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> domainDiscoveryReplyNotFound
    @Shared CasualNWMessageImpl<CasualEnqueueRequestMessage> actualEnqueueRequest
    @Shared CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> actualDomainDiscoveryRequest
    @Shared def bigBaddaBoom = 'big badda boom'
    @Shared int resourceId = 42

    def setup()
    {
        mcf = Mock(CasualManagedConnectionFactory)
        mcf.getResourceId() >> {
            resourceId
        }
        networkConnection = Mock(NetworkConnection)
        connection = new CasualManagedConnection( mcf )
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
        domainName = Domain.getName()
        queueName = 'echo'
        queueInfo = QueueInfo.createBuilder().withQueueName(queueName).build()
        message = JsonBuffer.of( "{msg: \"hello echo service.\"}" )
    }

    def initialiseExpectedRequests()
    {
        expectedEnqueueRequest = CasualEnqueueRequestMessage.createBuilder()
                                                            .withExecution(executionId)
                                                            .withXid(connection.getCurrentXid() )
                                                            .withQueueName(queueInfo.queueName)
                                                            .withMessage(EnqueueMessage.of(QueueMessage.of(message)))
                                                            .build()

        expectedDequeueRequest = CasualDequeueRequestMessage.createBuilder()
                                                            .withXid(connection.getCurrentXid())
                                                            .withExecution(executionId)
                                                            .withSelectorUUID(nullSelector.getSelectorId())
                                                            .withSelectorProperties(nullSelector.getSelector())
                                                            .withQueueName(queueInfo.queueName)
                                                            .withBlock(true)
                                                            .build()
        expectedDomainDiscoveryRequest = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                            .setQueueNames([queueInfo.queueName])
                                                                            .setDomainName(Domain.getName())
                                                                            .build()
    }

    def initialiseReplies()
    {
        enqueueReply = createEnqueueReplyMessage()
        dequeueReply = createDequeueReplyMessage()
        domainDiscoveryReplyFound = createDomainDiscoveryReply(asQueues([queueInfo.queueName]))
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

    CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> createDomainDiscoveryReply(List<Queue> queues)
    {
        CasualNWMessageImpl.of(executionId,
                           CasualDomainDiscoveryReplyMessage.of(executionId, domainId, domainName)
                                                            .setQueues(queues))
    }

    CasualNWMessageImpl<CasualEnqueueReplyMessage> createEnqueueReplyMessage()
    {
        CasualNWMessageImpl.of( executionId,
                CasualEnqueueReplyMessage.createBuilder()
                                         .withExecution(executionId)
                                         .withId(enqueueReplyId)
                                         .build())
    }

    CasualNWMessageImpl<CasualDequeueReplyMessage> createDequeueReplyMessage()
    {
        CasualNWMessageImpl.of(executionId,
                CasualDequeueReplyMessage.createBuilder()
                                         .withExecution(executionId)
                                         .withMessages(Arrays.asList(DequeueMessage.of(QueueMessage.of(message))))
                                         .build()
        )
    }

    def 'enqueue'()
    {
        when:
        EnqueueReturn msgId = instance.enqueue(queueInfo, QueueMessage.of(message))
        then:
        noExceptionThrown()
        msgId.getId().get() == enqueueReplyId
        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualEnqueueRequestMessage> input ->
                actualEnqueueRequest = input
                return new CompletableFuture<>(enqueueReply)
        }
        expect actualEnqueueRequest, matching( expectedEnqueueRequest )
    }

    def 'enqueue goes big badda boom'()
    {
        when:
        EnqueueReturn msgId = instance.enqueue(queueInfo, QueueMessage.of(message))
        then:
        null == msgId
        thrown(CasualConnectionException)
        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualEnqueueRequestMessage> input ->
                throw new RuntimeException(bigBaddaBoom)
        }
    }

    def 'dequeue goes big badda boom'()
    {
        when:
        DequeueReturn messages = instance.dequeue(queueInfo, MessageSelector.of())
        then:
        messages == null
        thrown(CasualConnectionException)
        1 * networkConnection.request(_) >> {
            CasualNWMessageImpl<CasualDequeueRequestMessage> input ->
                throw new RuntimeException(bigBaddaBoom)
        }
    }

    def 'queueExists'()
    {
        when:
        def r = instance.queueExists(queueInfo)
        then:
        r == true
        1 * networkConnection.request(_) >> {
            CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> input ->
                actualDomainDiscoveryRequest = input
                return new CompletableFuture<>(domainDiscoveryReplyFound)
        }
        expect actualDomainDiscoveryRequest, matching(expectedDomainDiscoveryRequest)
    }

    def 'queueExists - not found'()
    {
        when:
        def r = instance.queueExists(queueInfo)
        then:
        r == false
        1 * networkConnection.request(_) >> {
            CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> input ->
                actualDomainDiscoveryRequest = input
                return new CompletableFuture<>(domainDiscoveryReplyNotFound)
        }
        expect actualDomainDiscoveryRequest, matching(expectedDomainDiscoveryRequest)
    }

    def 'queueExists goes big badda boom'()
    {
        when:
        List<QueueMessage> messages = instance.queueExists(queueInfo)
        then:
        messages == null
        thrown(CasualConnectionException)
        1 * networkConnection.request(_) >> {
            CasualNWMessageImpl<CasualDequeueRequestMessage> input ->
                throw new RuntimeException(bigBaddaBoom)
        }
    }


}
