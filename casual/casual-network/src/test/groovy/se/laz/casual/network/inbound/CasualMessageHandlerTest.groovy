/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound

import io.netty.channel.ChannelHandlerContext
import jakarta.resource.spi.XATerminator
import jakarta.resource.spi.endpoint.MessageEndpointFactory
import jakarta.resource.spi.work.WorkManager
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.jca.inflow.CasualInboundTransactionRegistry
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.utils.FakeListener
import spock.lang.Shared
import spock.lang.Specification

class CasualMessageHandlerTest extends Specification
{
    @Shared
    def mockFactory = Mock(MessageEndpointFactory)
    @Shared
    def mockXATerminator = Mock(XATerminator)
    @Shared
    def mockWorkManager = Mock(WorkManager)
    @Shared
    CasualInboundTransactionRegistry inboundTransactionRegistry

    def setup()
    {
       inboundTransactionRegistry = new CasualInboundTransactionRegistry()
    }

    def 'test message routing'()
    {
        setup:
        def listener = Mock(FakeListener)
        def factory = Mock(MessageEndpointFactory)
        factory.createEndpoint(_) >> {
            return listener
        }
        def msg = Mock(CasualNWMessage)
        msg.getType() >> {
            msgType
        }
        def xaTerminator = Mock(XATerminator)
        def workManager = Mock(WorkManager)
        ProtocolVersionValueHolder valueHolder = ProtocolVersionValueHolder.of()
        valueHolder.accept(ProtocolVersion.VERSION_1_2)
        def instance = CasualMessageHandler.of(factory, xaTerminator, workManager, inboundTransactionRegistry, valueHolder)
        def ctx = Mock(ChannelHandlerContext)
        when:
        instance.channelRead0(ctx, msg)
        then:
        1 * listener."${methodName}"(*_)
        where:
        msgType                                      | methodName
        CasualNWMessageType.DOMAIN_CONNECT_REQUEST   | 'domainConnectRequest'
        CasualNWMessageType.DOMAIN_DISCOVERY_REQUEST | 'domainDiscoveryRequest'
        CasualNWMessageType.SERVICE_CALL_REQUEST     | 'serviceCallRequest'
        CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_3     | 'serviceCallRequest'
        CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_5     | 'serviceCallRequest'
        CasualNWMessageType.PREPARE_REQUEST          | 'prepareRequest'
        CasualNWMessageType.COMMIT_REQUEST           | 'commitRequest'
        CasualNWMessageType.REQUEST_ROLLBACK         | 'requestRollback'
    }

    def 'test failed construction'()
    {
        when:
        ProtocolVersionValueHolder valueHolder = ProtocolVersionValueHolder.of()
        valueHolder.accept(ProtocolVersion.VERSION_1_2)
        CasualMessageHandler.of(factory, xaTerminator, workManager, inboundTransactionRegistry, valueHolder)
        then:
        thrown(NullPointerException)
        where:
        factory     | xaTerminator     | workManager
        null        | mockXATerminator | mockWorkManager
        mockFactory | null             | mockWorkManager
        mockFactory | mockXATerminator | null
    }

}
