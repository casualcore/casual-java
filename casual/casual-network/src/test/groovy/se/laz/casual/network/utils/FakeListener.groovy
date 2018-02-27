package se.laz.casual.network.utils

import io.netty.channel.Channel
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage
import se.kodarkatten.casual.jca.inflow.CasualMessageListener
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import se.kodarkatten.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage

import javax.resource.spi.XATerminator
import javax.resource.spi.endpoint.MessageEndpoint
import javax.resource.spi.work.WorkManager
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