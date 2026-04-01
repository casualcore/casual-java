/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.utils

import io.netty.channel.Channel
import jakarta.resource.spi.XATerminator
import jakarta.resource.spi.endpoint.MessageEndpoint
import jakarta.resource.spi.work.WorkManager
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.jca.inflow.CasualInboundTransactionRegistry
import se.laz.casual.jca.inflow.CasualMessageListener
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectReplyMessage
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage

import java.lang.reflect.Method
import java.util.function.Consumer

class FakeListener implements MessageEndpoint, CasualMessageListener
{
    @Override
    void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException {

    }

    @Override
    void afterDelivery() throws ResourceException {

    }

    @Override
    void release() {

    }

    @Override
    void domainConnectRequest(CasualNWMessage<CasualDomainConnectRequestMessage> message, Channel channel, Consumer<ProtocolVersion> protocolVersion) {

    }

   @Override
   void domainDisconnectReply(CasualNWMessage<DomainDisconnectReplyMessage> message)
   {

   }

   @Override
    void domainDiscoveryRequest(CasualNWMessage<CasualDomainDiscoveryRequestMessage> message, Channel channel, ProtocolVersion protocolVersion) {

    }

    @Override
    void serviceCallRequest(CasualNWMessage<CasualServiceCallRequestMessage> message, Channel channel, WorkManager workManager, CasualInboundTransactionRegistry inboundTransactionRegistry, ProtocolVersion protocolVersion) {

    }

    @Override
    void prepareRequest(CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> message, Channel channel, XATerminator xaTerminator, CasualInboundTransactionRegistry inboundTransactionRegistry) {

    }

    @Override
    void commitRequest(CasualNWMessage<CasualTransactionResourceCommitRequestMessage> message, Channel channel, XATerminator xaTerminator, CasualInboundTransactionRegistry inboundTransactionRegistry) {

    }

    @Override
    void requestRollback(CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> message, Channel channel, XATerminator xaTerminator, CasualInboundTransactionRegistry inboundTransactionRegistry) {

    }
}