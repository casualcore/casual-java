/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.queue

import se.laz.casual.api.CasualRuntimeException
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.queue.DequeueReturn
import se.laz.casual.api.queue.EnqueueReturn
import se.laz.casual.api.queue.MessageSelector
import se.laz.casual.api.queue.QueueErrorCode
import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.api.queue.QueueMessage
import se.laz.casual.api.xa.XID
import se.laz.casual.config.json.Domain
import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.jca.CasualManagedConnection
import se.laz.casual.jca.CasualManagedConnectionFactory
import se.laz.casual.jca.CasualResourceManager
import se.laz.casual.jca.DomainId
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.connection.CasualConnectionException
import se.laz.casual.network.inbound.ProtocolVersionValueHolder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import se.laz.casual.network.protocol.messages.domain.Queue
import se.laz.casual.network.protocol.messages.queue.CasualDequeueReplyMessage
import se.laz.casual.network.protocol.messages.queue.CasualDequeueRequestMessage
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueReplyMessage
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueRequestMessage
import se.laz.casual.network.protocol.messages.queue.DequeueMessage
import se.laz.casual.network.protocol.messages.queue.EnqueueMessage
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3
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
    @Shared DomainId domainOne = DomainId.of(UUID.randomUUID())
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
    @Shared ProtocolVersionValueHolder protocolVersionValueHolder = ProtocolVersionValueHolder.of()
    QueueErrorCode queueErrorCode = QueueErrorCode.OK
    def setup()
    {
        mcf = Mock(CasualManagedConnectionFactory)
        mcf.getResourceId() >> {
            resourceId
        }
        networkConnection = Mock(NetworkConnection){
           getDomainId() >> domainOne
           getProtocolVersion() >> {protocolVersionValueHolder.get()}
        }
        connection = new CasualManagedConnection( mcf )
        connection.networkConnection =  networkConnection

        CasualResourceManager.getInstance().remove(domainOne, XID.NULL_XID)
        connection.getXAResource().start( XID.NULL_XID, 0 )
        CasualResourceManager.getInstance().remove(domainOne,XID.NULL_XID)

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
        queueInfo = QueueInfo.of(queueName)
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
        enqueueReply = createEnqueueReplyMessage(ProtocolVersion.VERSION_1_0)
        dequeueReply = createDequeueReplyMessage(ProtocolVersion.VERSION_1_0)
        domainDiscoveryReplyFound = createDomainDiscoveryReply(asQueues([queueInfo.queueName]), ProtocolVersion.VERSION_1_0)
        domainDiscoveryReplyNotFound = createDomainDiscoveryReply(asQueues([]), ProtocolVersion.VERSION_1_0)
    }

    List<Queue> asQueues(List<String> queuenames)
    {
        List<Queue> l = new ArrayList<>()
        for(String qname : queuenames)
        {
            l.add(Queue.createBuilder().withName(qname).withProtocolVersion( ProtocolVersion.VERSION_1_2).build())
        }
        return l
    }

    CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> createDomainDiscoveryReply(List<Queue> queues, ProtocolVersion protocolVersion)
    {
        CasualNWMessageImpl.of(executionId,
                           CasualDomainDiscoveryReplyMessage.of(executionId, domainId, domainName, protocolVersion)
                                                            .setQueues(queues))
    }

    CasualNWMessageImpl<CasualEnqueueReplyMessage> createEnqueueReplyMessage(ProtocolVersion protocolVersion)
    {
       CasualEnqueueReplyMessage.Builder builder = CasualEnqueueReplyMessage.createBuilder()
               .withExecution(executionId)
               .withId(enqueueReplyId)
               .withProtocolVersion(protocolVersion);
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) )
        {
           builder.withCode(queueErrorCode)
        }
        CasualNWMessageImpl.of( executionId,
                builder.build())
    }

    CasualNWMessageImpl<CasualDequeueReplyMessage> createDequeueReplyMessage(ProtocolVersion protocolVersion)
    {
        CasualNWMessageImpl.of(executionId,
                CasualDequeueReplyMessage.createBuilder()
                                         .withProtocolVersion(protocolVersion)
                                         .withExecution(executionId)
                                         .withMessages(Arrays.asList(DequeueMessage.of(QueueMessage.of(message))))
                                         .build()
        )
    }

    def 'enqueue'()
    {
        when:
        enqueueReply = createEnqueueReplyMessage(protocolVersion)
        protocolVersionValueHolder = new ProtocolVersionValueHolder()
        protocolVersionValueHolder.accept(protocolVersion)
        EnqueueReturn enqueueReturn = instance.enqueue(queueInfo, QueueMessage.of(message))
        then:
        noExceptionThrown()
        enqueueReturn.getId().get() == enqueueReplyId
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) )
        {
           QueueErrorCode code = enqueueReturn.getErrorCode().orElseThrow ({new CasualRuntimeException("Missing error code")})
           code == queueErrorCode
        }
        else
        {
           enqueueReturn.getErrorCode().isEmpty()
        }
        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualEnqueueRequestMessage> input ->
                actualEnqueueRequest = input
                return CompletableFuture.completedFuture(enqueueReply)
        }
        expect actualEnqueueRequest, matching( expectedEnqueueRequest )
        where:
        protocolVersion << ProtocolVersion.values()
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
                return CompletableFuture.completedFuture(domainDiscoveryReplyFound)
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
                return CompletableFuture.completedFuture(domainDiscoveryReplyNotFound)
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
