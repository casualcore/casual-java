/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException
import se.laz.casual.network.ProtocolVersion
import spock.lang.Specification
import spock.lang.Unroll

class MessageVerifierTest extends Specification
{
    @Unroll
    def 'DOMAIN_DISCOVERY_REPLY is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.DOMAIN_DISCOVERY_REPLY, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2, ProtocolVersion.VERSION_1_3]
    }

    def 'DOMAIN_DISCOVERY_REPLY is invalid for protocol version 1.4'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.DOMAIN_DISCOVERY_REPLY, () -> ProtocolVersion.VERSION_1_4)

        then:
        thrown(CasualProtocolException)
    }

    @Unroll
    def 'DOMAIN_DISCOVERY_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_FOUR is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.DOMAIN_DISCOVERY_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_FOUR, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_4]
    }

    @Unroll
    def 'DOMAIN_DISCOVERY_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_FOUR is invalid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.DOMAIN_DISCOVERY_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_FOUR, () -> protocolVersion)

        then:
        thrown(CasualProtocolException)

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2, ProtocolVersion.VERSION_1_3]
    }

    @Unroll
    def 'DOMAIN_DISCOVERY_TOPOLOGY_UPDATE is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.DOMAIN_DISCOVERY_TOPOLOGY_UPDATE, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_2, ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    @Unroll
    def 'DOMAIN_DISCOVERY_TOPOLOGY_UPDATE is invalid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.DOMAIN_DISCOVERY_TOPOLOGY_UPDATE, () -> protocolVersion)

        then:
        thrown(CasualProtocolException)

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1]
    }

    @Unroll
    def 'SERVICE_CALL_REQUEST is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.SERVICE_CALL_REQUEST, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2]
    }

    @Unroll
    def 'SERVICE_CALL_REQUEST is invalid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.SERVICE_CALL_REQUEST, () -> protocolVersion)

        then:
        thrown(CasualProtocolException)

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    @Unroll
    def 'SERVICE_CALL_REQUEST_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.SERVICE_CALL_REQUEST_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    @Unroll
    def 'SERVICE_CALL_REQUEST_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE is invalid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.SERVICE_CALL_REQUEST_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE, () -> protocolVersion)

        then:
        thrown(CasualProtocolException)

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2]
    }

    @Unroll
    def 'SERVICE_CALL_REPLY is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.SERVICE_CALL_REPLY, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2]
    }

    @Unroll
    def 'SERVICE_CALL_REPLY is invalid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.SERVICE_CALL_REPLY, () -> protocolVersion)

        then:
        thrown(CasualProtocolException)

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    @Unroll
    def 'SERVICE_CALL_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.SERVICE_CALL_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    @Unroll
    def 'SERVICE_CALL_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE is invalid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.SERVICE_CALL_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE, () -> protocolVersion)

        then:
        thrown(CasualProtocolException)

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2]
    }

    @Unroll
    def 'ENQUEUE_REPLY is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.ENQUEUE_REPLY, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2]
    }

    @Unroll
    def 'ENQUEUE_REPLY is invalid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.ENQUEUE_REPLY, () -> protocolVersion)

        then:
        thrown(CasualProtocolException)

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    @Unroll
    def 'ENQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.ENQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    @Unroll
    def 'ENQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE is invalid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.ENQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE, () -> protocolVersion)

        then:
        thrown(CasualProtocolException)

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2]
    }

    @Unroll
    def 'DEQUEUE_REPLY is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.DEQUEUE_REPLY, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2]
    }

    @Unroll
    def 'DEQUEUE_REPLY is invalid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.DEQUEUE_REPLY, () -> protocolVersion)

        then:
        thrown(CasualProtocolException)

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    @Unroll
    def 'DEQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.DEQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    @Unroll
    def 'DEQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE is invalid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.DEQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE, () -> protocolVersion)

        then:
        thrown(CasualProtocolException)

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2]
    }

    @Unroll
    def 'CONVERSATION_CONNECT is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.CONVERSATION_CONNECT, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2]
    }

    @Unroll
    def 'CONVERSATION_CONNECT is invalid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.CONVERSATION_CONNECT, () -> protocolVersion)

        then:
        thrown(CasualProtocolException)

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    @Unroll
    def 'CONVERSATION_CONNECT_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE is valid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.CONVERSATION_CONNECT_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    @Unroll
    def 'CONVERSATION_CONNECT_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE is invalid for protocol version #protocolVersion'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(CasualNWMessageType.CONVERSATION_CONNECT_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE, () -> protocolVersion)

        then:
        thrown(CasualProtocolException)

        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2]
    }

    @Unroll
    def 'version-agnostic message type #messageType passes for all protocol versions'()
    {
        when:
        MessageVerifier.verifyMessageTypeByProtocolVersion(messageType, () -> protocolVersion)

        then:
        noExceptionThrown()

        where:
        [messageType, protocolVersion] << [
                [CasualNWMessageType.DOMAIN_CONNECT_REQUEST,
                 CasualNWMessageType.DOMAIN_CONNECT_REPLY,
                 CasualNWMessageType.DOMAIN_DISCONNECT_REQUEST,
                 CasualNWMessageType.DOMAIN_DISCONNECT_REPLY,
                 CasualNWMessageType.DOMAIN_DISCOVERY_REQUEST,
                 CasualNWMessageType.ENQUEUE_REQUEST,
                 CasualNWMessageType.DEQUEUE_REQUEST,
                 CasualNWMessageType.PREPARE_REQUEST,
                 CasualNWMessageType.PREPARE_REQUEST_REPLY,
                 CasualNWMessageType.COMMIT_REQUEST,
                 CasualNWMessageType.COMMIT_REQUEST_REPLY,
                 CasualNWMessageType.REQUEST_ROLLBACK,
                 CasualNWMessageType.REQUEST_ROLLBACK_REPLY,
                 CasualNWMessageType.CONVERSATION_CONNECT_REPLY,
                 CasualNWMessageType.CONVERSATION_REQUEST,
                 CasualNWMessageType.CONVERSATION_DISCONNECT],
                ProtocolVersion.values() as List
        ].combinations()
    }
}
