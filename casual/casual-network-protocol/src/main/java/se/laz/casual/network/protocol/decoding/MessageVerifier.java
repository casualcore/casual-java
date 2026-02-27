/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.network.ProtocolVersion;

import java.util.function.Supplier;

import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.CONVERSATION_CONNECT;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.CONVERSATION_CONNECT_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.DEQUEUE_REPLY;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.DEQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.DOMAIN_DISCOVERY_REPLY;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.DOMAIN_DISCOVERY_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_FOUR;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.DOMAIN_DISCOVERY_TOPOLOGY_UPDATE;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.ENQUEUE_REPLY;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.ENQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.SERVICE_CALL_REPLY;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.SERVICE_CALL_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.SERVICE_CALL_REQUEST;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.SERVICE_CALL_REQUEST_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE;

// it is not brain overload
// we do not want a default case here
@SuppressWarnings({"java:S3776", "java:S131"})
public final class MessageVerifier
{
    private MessageVerifier()
    {}

    public static void verifyMessageTypeByProtocolVersion(CasualNWMessageType messageType, Supplier<ProtocolVersion> protocolVersion)
    {
        // we check the protocol version and message type for messages that are available in different versions
        switch(messageType)
        {
            // 1.0 - 1.3
            case DOMAIN_DISCOVERY_REPLY:
                if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  DOMAIN_DISCOVERY_REPLY.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
            // 1.4 -
            case DOMAIN_DISCOVERY_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_FOUR:
                if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  DOMAIN_DISCOVERY_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_FOUR.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
            // 1.2 -
            case DOMAIN_DISCOVERY_TOPOLOGY_UPDATE:
                if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneTwo(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  DOMAIN_DISCOVERY_TOPOLOGY_UPDATE.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
            // 1.0 - 1.2
            case SERVICE_CALL_REQUEST:
                if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  SERVICE_CALL_REQUEST.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
            // 1.3 -
            case SERVICE_CALL_REQUEST_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE:
                if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  SERVICE_CALL_REQUEST_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
            // 1.0 - 1.2
            case SERVICE_CALL_REPLY:
                if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  SERVICE_CALL_REPLY.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
            // 1.3 -
            case SERVICE_CALL_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE:
                if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  SERVICE_CALL_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
            // 1.0 - 1.2
            case ENQUEUE_REPLY:
                if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  ENQUEUE_REPLY.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
            // 1.3 -
            case ENQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE:
                if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  ENQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
            // 1.0 - 1.2
            case DEQUEUE_REPLY:
                if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  DEQUEUE_REPLY.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
            // 1.3 -
            case DEQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE:
                if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  DEQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
            // 1.0 - 1.2
            case CONVERSATION_CONNECT:
                if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  CONVERSATION_CONNECT.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
            // 1.3 -
            case CONVERSATION_CONNECT_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE:
                if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion.get()))
                {
                    throw new CasualProtocolException("Message type " +  CONVERSATION_CONNECT_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
                }
                break;
        }

    }

}
