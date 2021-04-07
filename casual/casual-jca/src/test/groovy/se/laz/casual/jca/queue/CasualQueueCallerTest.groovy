/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.queue

import com.google.protobuf.ByteString
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.queue.MessageSelector
import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.api.queue.QueueMessage
import se.laz.casual.api.xa.XID
import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.jca.CasualManagedConnection
import se.laz.casual.jca.CasualManagedConnectionFactory
import se.laz.casual.jca.CasualResourceManager
import se.laz.casual.network.connection.CasualConnectionException
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualDequeueReply
import se.laz.casual.network.messages.CasualDequeueRequest
import se.laz.casual.network.messages.CasualDomainDiscoveryReply
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest
import se.laz.casual.network.messages.CasualEnqueueReply
import se.laz.casual.network.messages.CasualEnqueueRequest
import se.laz.casual.network.messages.CasualReply
import se.laz.casual.network.messages.CasualRequest
import se.laz.casual.network.messages.DequeueMessage
import se.laz.casual.network.messages.Selector
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
    @Shared String domainName
    @Shared String queueName
    @Shared def queueSpace
    @Shared QueueInfo queueInfo
    @Shared MessageSelector nullSelector = MessageSelector.of()
    @Shared JsonBuffer message
    @Shared CasualEnqueueRequest expectedEnqueueRequest
    @Shared CasualDequeueRequest expectedDequeueRequest
    @Shared CasualDomainDiscoveryRequest expectedDomainDiscoveryRequest
    @Shared CasualReply enqueueReply
    @Shared CasualReply dequeueReply
    @Shared CasualReply domainDiscoveryReplyFound
    @Shared CasualReply domainDiscoveryReplyNotFound
    @Shared CasualRequest actualEnqueueRequest
    @Shared CasualRequest actualDomainDiscoveryRequest
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
        domainName = connection.getDomainName()
        queueName = 'echo'
        queueSpace = 'asdf'
        queueInfo = QueueInfo.createBuilder().withQspace(queueSpace).withQname(queueName).build()
        message = JsonBuffer.of( "{msg: \"hello echo service.\"}" )
    }

    def initialiseExpectedRequests()
    {
        expectedEnqueueRequest = CasualEnqueueRequest.newBuilder()
                .setExecution(MessageCreator.toUUID4(executionId))
                .setXid(MessageCreator.toXID(connection.getCurrentXid()))
                .setQueueName(queueInfo.getCompositeName())
                .setMessage(se.laz.casual.network.messages.QueueMessage.newBuilder()
                        .setPayload(ByteString.copyFrom(message.getBytes().get(0)))
                        .setType(message.getType())
                        .build())
                .build()

        expectedDequeueRequest = CasualDequeueRequest.newBuilder()
                .setExecution(MessageCreator.toUUID4(executionId))
                .setXid(MessageCreator.toXID(connection.getCurrentXid()))
                .setSelector(Selector.newBuilder()
                .setProperties('')
                .setId(MessageCreator.toUUID4(new UUID(0,0))))
                .setQueueName(queueInfo.getCompositeName())
                .setBlock(true)
                .build()

        expectedDomainDiscoveryRequest = CasualDomainDiscoveryRequest.newBuilder()
                .addAllQueueNames([queueInfo.compositeName])
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

    List<se.laz.casual.network.messages.Queue> asQueues(List<String> queuenames)
    {
        List<se.laz.casual.network.messages.Queue> l = new ArrayList<>()
        for(String qname : queuenames)
        {
            l.add(se.laz.casual.network.messages.Queue.newBuilder()
            .setName(qname)
            .build())
        }
        return l
    }

    CasualReply createDomainDiscoveryReply(List<se.laz.casual.network.messages.Queue> queues)
    {
        CasualDomainDiscoveryReply reply = CasualDomainDiscoveryReply.newBuilder()
                .setExecution(MessageCreator.toUUID4(executionId))
                .setDomainId(MessageCreator.toUUID4(domainId))
                .setDomainName(domainName)
                .addAllQueues(queues)
                .build()
        return CasualReply.newBuilder()
                .setMessageType(CasualReply.MessageType.DOMAIN_DISCOVERY_REPLY)
                .setCorrelationId(MessageCreator.toUUID4(executionId))
                .setDomainDiscovery(reply)
                .build()
    }

    CasualReply createEnqueueReplyMessage()
    {
        CasualEnqueueReply reply = CasualEnqueueReply.newBuilder()
        .setExecution(MessageCreator.toUUID4(executionId))
        .setMessageId(MessageCreator.toUUID4(enqueueReplyId))
        .build()
        return CasualReply.newBuilder()
                .setMessageType(CasualReply.MessageType.ENQUEUE_REPLY)
                .setCorrelationId(MessageCreator.toUUID4(executionId))
                .setEnqueue(reply)
                .build()
    }

    CasualReply createDequeueReplyMessage()
    {
        CasualDequeueReply reply = CasualDequeueReply.newBuilder()
                .setExecution(MessageCreator.toUUID4(executionId))
                .addAllMessage([DequeueMessage.newBuilder()
                                        .setPayload(ByteString.copyFrom(message.getBytes().get(0)))
                                        .setType(message.getType())
                                        .build()])
                .build()
        return CasualReply.newBuilder()
                .setMessageType(CasualReply.MessageType.DEQUEUE_REPLY)
                .setCorrelationId(MessageCreator.toUUID4(executionId))
                .setDequeue(reply)
                .build()
    }

    def 'enqueue'()
    {
        when:
        UUID msgId = instance.enqueue(queueInfo, QueueMessage.of(message))
        then:
        noExceptionThrown()
        msgId == enqueueReplyId
        1 * networkConnection.request( _ ) >> {
            CasualRequest input ->
                actualEnqueueRequest = input
                return new CompletableFuture<>(enqueueReply)
        }
        expect actualEnqueueRequest.getEnqueue(), matching( expectedEnqueueRequest )
    }

    def 'enqueue goes big badda boom'()
    {
        when:
        UUID msgId = instance.enqueue(queueInfo, QueueMessage.of(message))
        then:
        null == msgId
        thrown(CasualConnectionException)
        1 * networkConnection.request( _ ) >> {
            CasualRequest input ->
                throw new RuntimeException(bigBaddaBoom)
        }
    }

    def 'dequeue goes big badda boom'()
    {
        when:
        List<QueueMessage> messages = instance.dequeue(queueInfo, MessageSelector.of())
        then:
        messages == null
        thrown(CasualConnectionException)
        1 * networkConnection.request(_) >> {
            CasualRequest input ->
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
            CasualRequest input ->
                actualDomainDiscoveryRequest = input
                return new CompletableFuture<>(domainDiscoveryReplyFound)
        }
        expect actualDomainDiscoveryRequest.getDomainDiscovery(), matching(expectedDomainDiscoveryRequest)
    }

    def 'queueExists - not found'()
    {
        when:
        def r = instance.queueExists(queueInfo)
        then:
        r == false
        1 * networkConnection.request(_) >> {
            CasualRequest input ->
                actualDomainDiscoveryRequest = input
                return new CompletableFuture<>(domainDiscoveryReplyNotFound)
        }
        expect actualDomainDiscoveryRequest.getDomainDiscovery(), matching(expectedDomainDiscoveryRequest)
    }

    def 'queueExists goes big badda boom'()
    {
        when:
        List<QueueMessage> messages = instance.queueExists(queueInfo)
        then:
        messages == null
        thrown(CasualConnectionException)
        1 * networkConnection.request(_) >> {
            CasualRequest input ->
                throw new RuntimeException(bigBaddaBoom)
        }
    }


}
