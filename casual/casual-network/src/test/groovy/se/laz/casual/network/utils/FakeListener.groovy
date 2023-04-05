/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.utils

import io.netty.channel.Channel
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.jca.inflow.CasualMessageListener
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage

import jakarta.resource.spi.XATerminator
import jakarta.resource.spi.endpoint.MessageEndpoint
import jakarta.resource.spi.work.WorkManager
import java.lang.reflect.Method

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
    void domainConnectRequest(CasualNWMessage<CasualDomainConnectRequestMessage> message, Channel channel) {

    }

    @Override
    void domainDiscoveryRequest(CasualNWMessage<CasualDomainDiscoveryRequestMessage> message, Channel channel) {

    }

    @Override
    void serviceCallRequest(CasualNWMessage<CasualServiceCallRequestMessage> message, Channel channel, WorkManager workManager) {

    }

    @Override
    void prepareRequest(CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> message, Channel channel, XATerminator xaTerminator) {

    }

    @Override
    void commitRequest(CasualNWMessage<CasualTransactionResourceCommitRequestMessage> message, Channel channel, XATerminator xaTerminator) {

    }

    @Override
    void requestRollback(CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> message, Channel channel, XATerminator xaTerminator) {

    }
}