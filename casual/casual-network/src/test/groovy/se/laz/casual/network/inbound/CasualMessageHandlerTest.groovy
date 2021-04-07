/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound

import io.netty.channel.ChannelHandlerContext
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.network.messages.CasualRequest
import se.laz.casual.network.utils.FakeListener
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.XATerminator
import javax.resource.spi.endpoint.MessageEndpointFactory
import javax.resource.spi.work.WorkManager

class CasualMessageHandlerTest extends Specification
{
    @Shared
    def mockFactory = Mock(MessageEndpointFactory)
    @Shared
    def mockXATerminator = Mock(XATerminator)
    @Shared
    def mockWorkManager = Mock(WorkManager)

    def 'test message routing'()
    {
        setup:
        def listener = Mock(FakeListener)
        def factory = Mock(MessageEndpointFactory)
        factory.createEndpoint(_) >> {
            return listener
        }
        def msg = CasualRequest.newBuilder()
                .setMessageType(msgType)
                .build()
        def xaTerminator = Mock(XATerminator)
        def workManager = Mock(WorkManager)
        def instance = CasualMessageHandler.of(factory, xaTerminator, workManager)
        def ctx = Mock(ChannelHandlerContext)
        when:
        instance.channelRead0(ctx, msg)
        then:
        1 * listener."${methodName}"(*_)
        where:
        msgType                                      | methodName
        CasualRequest.MessageType.DOMAIN_CONNECT_REQUEST   | 'domainConnectRequest'
        CasualRequest.MessageType.DOMAIN_DISCOVERY_REQUEST | 'domainDiscoveryRequest'
        CasualRequest.MessageType.SERVICE_CALL_REQUEST     | 'serviceCallRequest'
        CasualRequest.MessageType.PREPARE_REQUEST          | 'prepareRequest'
        CasualRequest.MessageType.COMMIT_REQUEST           | 'commitRequest'
        CasualRequest.MessageType.ROLLBACK_REQUEST         | 'requestRollback'
    }

    def 'test failed construction'()
    {
        when:
        CasualMessageHandler.of(factory, xaTerminator, workManager)
        then:
        thrown(NullPointerException)
        where:
        factory     | xaTerminator     | workManager
        null        | mockXATerminator | mockWorkManager
        mockFactory | null             | mockWorkManager
        mockFactory | mockXATerminator | null
    }

}
