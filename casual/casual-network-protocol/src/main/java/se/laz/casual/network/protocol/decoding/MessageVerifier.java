/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.network.ProtocolVersion;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.CONVERSATION_CONNECT;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.CONVERSATION_CONNECT_V_1_3;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.DEQUEUE_REPLY;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.DEQUEUE_REPLY_V_1_3;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.DOMAIN_DISCOVERY_REPLY;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.DOMAIN_DISCOVERY_REPLY_V_1_4;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.DOMAIN_DISCOVERY_TOPOLOGY_UPDATE;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.ENQUEUE_REPLY;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.ENQUEUE_REPLY_V_1_3;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.SERVICE_CALL_REPLY;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.SERVICE_CALL_REPLY_V_1_3;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.SERVICE_CALL_REPLY_V_1_5;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.SERVICE_CALL_REQUEST;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_3;
import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_5;

/**
 * Verification of network messages to ensure messages sent after protocol handshake are as expected.
 * <br>
 * For example version 1.3 service call should not return version 1.2 service reply.
 */
public final class MessageVerifier
{

    /**
     *  Map containing message types that have changed in newer gateway protocol versions, with a set of the protocol versions in which the message type is that are valid.
     *  Absence from the map means that the message type is valid in all versions.
     *  NB: When adding a new protocol version, ensure to update the existing entries in the map to include the newest protocol version where applicable.
     */
    private static final EnumMap<CasualNWMessageType, Set<ProtocolVersion>> messageProtocolVersions;

    static
    {
        messageProtocolVersions = new EnumMap<>(CasualNWMessageType.class);
        //1.0 - 1.3
        messageProtocolVersions.put( DOMAIN_DISCOVERY_REPLY, createSet( ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2, ProtocolVersion.VERSION_1_3 ) );
        // 1.4 -
        messageProtocolVersions.put(DOMAIN_DISCOVERY_REPLY_V_1_4, createSet( ProtocolVersion.VERSION_1_4, ProtocolVersion.VERSION_1_5 ) );

        // 1.2 -
        messageProtocolVersions.put( DOMAIN_DISCOVERY_TOPOLOGY_UPDATE, createSet( ProtocolVersion.VERSION_1_2, ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4, ProtocolVersion.VERSION_1_5 ) );

        // 1.0 - 1.2
        messageProtocolVersions.put( SERVICE_CALL_REQUEST, createSet( ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2 ) );
        // 1.3 - 1.4
        messageProtocolVersions.put(SERVICE_CALL_REQUEST_V_1_3, createSet( ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4 ) );
        // 1.5 -
        messageProtocolVersions.put( SERVICE_CALL_REQUEST_V_1_5, createSet( ProtocolVersion.VERSION_1_5 ) );

        // 1.0 - 1.2
        messageProtocolVersions.put( SERVICE_CALL_REPLY, createSet( ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2 ) );
        // 1.3 - 1.4
        messageProtocolVersions.put(SERVICE_CALL_REPLY_V_1_3, createSet( ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4 ) );
        // 1.5
        messageProtocolVersions.put(SERVICE_CALL_REPLY_V_1_5, createSet( ProtocolVersion.VERSION_1_5 ) );

        // 1.0 - 1.2
        messageProtocolVersions.put( ENQUEUE_REPLY, createSet( ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2 ) );
        // 1.3 -
        messageProtocolVersions.put(ENQUEUE_REPLY_V_1_3, createSet( ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4, ProtocolVersion.VERSION_1_5 ) );


        // 1.0 - 1.2
        messageProtocolVersions.put( DEQUEUE_REPLY, createSet( ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2 ) );
        // 1.3 -
        messageProtocolVersions.put(DEQUEUE_REPLY_V_1_3, createSet( ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4, ProtocolVersion.VERSION_1_5 ) );

        // 1.0 - 1.2
        messageProtocolVersions.put( CONVERSATION_CONNECT, createSet( ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2 ) );
        // 1.3 -
        messageProtocolVersions.put(CONVERSATION_CONNECT_V_1_3, createSet( ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4, ProtocolVersion.VERSION_1_5 ) );
    }

    private static Set<ProtocolVersion> createSet( ProtocolVersion... versions )
    {
        return new HashSet<>( Set.of( versions ) );
    }

    private MessageVerifier()
    {}

    /**
     * Verify that the network message is valid for the specified protocol version.
     *
     * @param messageType to verify.
     * @param protocolVersion to verify against.
     */
    public static void verifyMessageTypeByProtocolVersion( CasualNWMessageType messageType, Supplier<ProtocolVersion> protocolVersion )
    {
        if( messageProtocolVersions.containsKey( messageType ) &&
                ! messageProtocolVersions.get( messageType ).contains( protocolVersion.get() ))
        {
            throw new CasualProtocolException("Message type " +  messageType.getMessageId() + " is not supported by protocol version " + protocolVersion.get());
        }
    }
}
