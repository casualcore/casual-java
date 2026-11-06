/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.api.xa.XID
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class CasualMessageHandlerTest extends Specification
{
    @Shared
    def correlator = CorrelatorImpl.of()
    @Shared
    def corrid = UUID.randomUUID()
    @Shared
    def instance = CasualMessageHandler.of(correlator)

    def 'failed construction'()
    {
        when:
        CasualMessageHandler h = CasualMessageHandler.of(null)
        then:
        thrown(NullPointerException)
    }

    def 'ok'()
    {
        setup:
        instance.setMessageListener(Mock(CasualOutboundMessageListener))
        CompletableFuture<CasualNWMessage<CasualNWMessage<CasualServiceCallReplyMessage>>> f = new CompletableFuture<>()
        CasualNWMessage<CasualServiceCallReplyMessage> msg = createReplyMessage()
        correlator.put(corrid, f)
        when:
        instance.channelRead0(null, msg)
        def r = f.get()
        then:
        noExceptionThrown()
        r == msg
    }

    def 'connect reply completes as a correlated response'()
    {
        given:
        Correlator localCorrelator = Mock()
        CasualOutboundMessageListener listener = Mock()
        CasualMessageHandler handler = CasualMessageHandler.of(localCorrelator)
        UUID remoteDomainId = UUID.randomUUID()
        handler.setMessageListener(listener)
        CasualNWMessage<CasualDomainConnectReplyMessage> connectReply = createConnectReply(remoteDomainId)

        when:
        handler.channelRead0(null, connectReply)

        then:
        1 * listener.isInterestedIn(CasualNWMessageType.DOMAIN_CONNECT_REPLY) >> false
        1 * localCorrelator.complete(connectReply)
        0 * listener.handleMessage(_)
    }

    private static CasualNWMessage<CasualDomainConnectReplyMessage> createConnectReply(UUID remoteDomainId)
    {
        return CasualNWMessageImpl.of(
                UUID.randomUUID(),
                CasualDomainConnectReplyMessage.createBuilder()
                        .withExecution(UUID.randomUUID())
                        .withDomainId(remoteDomainId)
                        .withDomainName('remote-domain')
                        .withProtocolVersion(ProtocolVersion.VERSION_1_5.version)
                        .build())
    }

    def createReplyMessage()
    {
        CasualBuffer msg = JsonBuffer.of('{"msg": "hello world"}')
        CasualServiceCallReplyMessage message = CasualServiceCallReplyMessage.createBuilder()
                .setTransactionState(TransactionState.TX_ACTIVE)
                .setUserSuppliedError(0)
                .setError(ErrorState.OK)
                .setXid(XID.NULL_XID)
                .setExecution(UUID.randomUUID())
                .setServiceBuffer(ServiceBuffer.of(msg))
                .setProtocolVersion(ProtocolVersion.VERSION_1_2)
                .build()

        return CasualNWMessageImpl.of(corrid, message)
    }
}
