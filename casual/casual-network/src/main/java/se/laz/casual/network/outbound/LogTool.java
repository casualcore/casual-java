/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.util.PrettyPrinter;
import se.laz.casual.network.protocol.messages.conversation.ConnectReply;
import se.laz.casual.network.protocol.messages.conversation.ConnectRequest;
import se.laz.casual.network.protocol.messages.conversation.Disconnect;
import se.laz.casual.network.protocol.messages.conversation.Request;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.queue.CasualDequeueReplyMessage;
import se.laz.casual.network.protocol.messages.queue.CasualDequeueRequestMessage;
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueReplyMessage;
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueRequestMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitReplyMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareReplyMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackReplyMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import java.util.UUID;

public final class LogTool
{
    private LogTool()
    {}

    public static <X extends CasualNetworkTransmittable> String asLogEntry(CasualNWMessage<X> message)
    {
        UUID execution;
        switch(message.getType())
        {
            case DOMAIN_CONNECT_REQUEST:
                execution = ((CasualDomainConnectRequestMessage)message.getMessage()).getExecution();
                break;
            case DOMAIN_CONNECT_REPLY:
                execution = ((CasualDomainConnectReplyMessage)message.getMessage()).getExecution();
                break;
            case DOMAIN_DISCOVERY_REQUEST:
                execution = ((CasualDomainDiscoveryRequestMessage)message.getMessage()).getExecution();
                break;
            case DOMAIN_DISCOVERY_REPLY:
                execution = ((CasualDomainDiscoveryReplyMessage)message.getMessage()).getExecution();
                break;
            case SERVICE_CALL_REQUEST:
                execution = ((CasualServiceCallRequestMessage)message.getMessage()).getExecution();
                break;
            case SERVICE_CALL_REPLY:
                execution = ((CasualServiceCallReplyMessage)message.getMessage()).getExecution();
                break;
            case ENQUEUE_REQUEST:
                execution = ((CasualEnqueueRequestMessage)message.getMessage()).getExecution();
                break;
            case ENQUEUE_REPLY:
                execution = ((CasualEnqueueReplyMessage)message.getMessage()).getExecution();
                break;
            case DEQUEUE_REQUEST:
                execution = ((CasualDequeueRequestMessage)message.getMessage()).getExecution();
                break;
            case DEQUEUE_REPLY:
                execution = ((CasualDequeueReplyMessage)message.getMessage()).getExecution();
                break;
            case PREPARE_REQUEST:
                execution = ((CasualTransactionResourcePrepareRequestMessage)message.getMessage()).getExecution();
                break;
            case PREPARE_REQUEST_REPLY:
                execution = ((CasualTransactionResourcePrepareReplyMessage)message.getMessage()).getExecution();
                break;
            case COMMIT_REQUEST:
                execution = ((CasualTransactionResourceCommitRequestMessage)message.getMessage()).getExecution();
                break;
            case COMMIT_REQUEST_REPLY:
                execution = ((CasualTransactionResourceCommitReplyMessage)message.getMessage()).getExecution();
                break;
            case REQUEST_ROLLBACK:
                execution = ((CasualTransactionResourceRollbackRequestMessage)message.getMessage()).getExecution();
                break;
            case REQUEST_ROLLBACK_REPLY:
                execution = ((CasualTransactionResourceRollbackReplyMessage)message.getMessage()).getExecution();
                break;
            case CONVERSATION_CONNECT:
                execution = ((ConnectRequest)message.getMessage()).getExecution();
                break;
            case CONVERSATION_CONNECT_REPLY:
                execution = ((ConnectReply)message.getMessage()).getExecution();
                break;
            case CONVERSATION_REQUEST:
                execution = ((Request)message.getMessage()).getExecution();
                break;
            case CONVERSATION_DISCONNECT:
                execution = ((Disconnect)message.getMessage()).getExecution();
                break;
            default:
                return String.format("Unknown message type: %s", message.getType().name());
        }
        return String.format("Message type: %s, execution: %s, corrid: %s",
                message.getType().name(),
                PrettyPrinter.casualStringify(execution),
                PrettyPrinter.casualStringify(message.getCorrelationId()));
    }
}
