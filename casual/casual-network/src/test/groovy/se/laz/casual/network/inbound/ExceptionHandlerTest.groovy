/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelId
import se.laz.casual.jca.inflow.CasualInboundTransactionRegistry
import spock.lang.Specification

class ExceptionHandlerTest extends Specification
{
    def 'should close context'()
    {
        setup:
        CasualInboundTransactionRegistry inboundTransactionRegistry = new CasualInboundTransactionRegistry()
        def ctx = Mock(ChannelHandlerContext){
           channel() >> Mock(Channel){
              id() >> Mock(ChannelId)
           }
        }
        def handler = ExceptionHandler.of(inboundTransactionRegistry)
        when:
        handler.exceptionCaught(ctx, new RuntimeException())
        then:
        1 * ctx.close()
    }
}
