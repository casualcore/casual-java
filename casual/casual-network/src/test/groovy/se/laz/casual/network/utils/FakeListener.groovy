/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.utils

import io.netty.channel.Channel
import se.laz.casual.jca.inflow.CasualMessageListener
import se.laz.casual.network.messages.CasualRequest

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
    void domainConnectRequest(CasualRequest message, Channel channel) {

    }

    @Override
    void domainDiscoveryRequest(CasualRequest message, Channel channel) {

    }

    @Override
    void serviceCallRequest(CasualRequest message, Channel channel, WorkManager workManager) {

    }

    @Override
    void prepareRequest(CasualRequest message, Channel channel, XATerminator xaTerminator) {

    }

    @Override
    void commitRequest(CasualRequest message, Channel channel, XATerminator xaTerminator) {

    }

    @Override
    void requestRollback(CasualRequest message, Channel channel, XATerminator xaTerminator) {

    }
}