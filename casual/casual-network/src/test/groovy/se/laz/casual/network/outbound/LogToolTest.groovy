/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable
import se.laz.casual.api.util.PrettyPrinter
import se.laz.casual.network.protocol.messages.conversation.ConnectReply
import se.laz.casual.network.protocol.messages.conversation.ConnectRequest
import se.laz.casual.network.protocol.messages.conversation.Disconnect
import se.laz.casual.network.protocol.messages.conversation.Request
import se.laz.casual.network.protocol.messages.domain.*
import se.laz.casual.network.protocol.messages.queue.CasualDequeueReplyMessage
import se.laz.casual.network.protocol.messages.queue.CasualDequeueRequestMessage
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueReplyMessage
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueRequestMessage
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import se.laz.casual.network.protocol.messages.transaction.*
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class LogToolTest extends Specification
{
    @Shared
    UUID execution = UUID.randomUUID()
    @Shared
    UUID corrid = UUID.randomUUID()
    @Unroll
    def 'ok log entry for #messageType'()
    {
        given:
        CasualNWMessage<CasualNetworkTransmittable> envelope = Mock(CasualNWMessage){
            getMessage() >> createMessage(messageType)
            getType() >> messageType
            getCorrelationId() >> corrid
        }
        expect:
        LogTool.asLogEntry(envelope).contains(PrettyPrinter.casualStringify(execution))
        LogTool.asLogEntry(envelope).contains(PrettyPrinter.casualStringify(corrid))
        where:
        messageType << CasualNWMessageType.values()
    }


    CasualNetworkTransmittable createMessage(CasualNWMessageType messageType)
    {
        switch(messageType)
        {
            case CasualNWMessageType.DOMAIN_CONNECT_REQUEST:
                CasualDomainConnectRequestMessage message = Mock(CasualDomainConnectRequestMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.DOMAIN_CONNECT_REPLY:
                CasualDomainConnectReplyMessage message = Mock(CasualDomainConnectReplyMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.DOMAIN_DISCONNECT_REQUEST:
                DomainDisconnectRequestMessage message = Mock(DomainDisconnectRequestMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.DOMAIN_DISCONNECT_REPLY:
                DomainDisconnectReplyMessage message = Mock(DomainDisconnectReplyMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.DOMAIN_DISCOVERY_REQUEST:
                CasualDomainDiscoveryRequestMessage message = Mock(CasualDomainDiscoveryRequestMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.DOMAIN_DISCOVERY_REPLY:
                CasualDomainDiscoveryReplyMessage message = Mock(CasualDomainDiscoveryReplyMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.DOMAIN_DISCOVERY_TOPOLOGY_UPDATE:
                DomainDiscoveryTopologyUpdateMessage message = Mock(DomainDiscoveryTopologyUpdateMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.SERVICE_CALL_REQUEST:
                CasualServiceCallRequestMessage message = Mock(CasualServiceCallRequestMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.SERVICE_CALL_REPLY:
                CasualServiceCallReplyMessage message = Mock(CasualServiceCallReplyMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.ENQUEUE_REQUEST:
                CasualEnqueueRequestMessage message = Mock(CasualEnqueueRequestMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.ENQUEUE_REPLY:
                CasualEnqueueReplyMessage message = Mock(CasualEnqueueReplyMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.DEQUEUE_REQUEST:
                CasualDequeueRequestMessage message = Mock(CasualDequeueRequestMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.DEQUEUE_REPLY:
                CasualDequeueReplyMessage message = Mock(CasualDequeueReplyMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.PREPARE_REQUEST:
                CasualTransactionResourcePrepareRequestMessage message = Mock(CasualTransactionResourcePrepareRequestMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.PREPARE_REQUEST_REPLY:
                CasualTransactionResourcePrepareReplyMessage message = Mock(CasualTransactionResourcePrepareReplyMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.COMMIT_REQUEST:
                CasualTransactionResourceCommitRequestMessage message = Mock(CasualTransactionResourceCommitRequestMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.COMMIT_REQUEST_REPLY:
                CasualTransactionResourceCommitReplyMessage message = Mock(CasualTransactionResourceCommitReplyMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.REQUEST_ROLLBACK:
                CasualTransactionResourceRollbackRequestMessage message = Mock(CasualTransactionResourceRollbackRequestMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.REQUEST_ROLLBACK_REPLY:
                CasualTransactionResourceRollbackReplyMessage message = Mock(CasualTransactionResourceRollbackReplyMessage){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.CONVERSATION_CONNECT:
                ConnectRequest message = Mock(ConnectRequest){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.CONVERSATION_CONNECT_REPLY:
                ConnectReply message = Mock(ConnectReply){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.CONVERSATION_REQUEST:
                Request message = Mock(Request){
                    getExecution() >> execution
                }
                return message
            case CasualNWMessageType.CONVERSATION_DISCONNECT:
                Disconnect message = Mock(Disconnect){
                    getExecution() >> execution
                }
                return message
            default:
                throw new RuntimeException("Unknown message type: " + messageType)
        }
    }
}
