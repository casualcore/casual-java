/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound

import io.netty.channel.ChannelHandlerContext
import spock.lang.Specification

class ExceptionHandlerTest extends Specification
{
    def 'should close context'()
    {
        setup:
        def ctx = Mock(ChannelHandlerContext)
        def handler = ExceptionHandler.of()
        when:
        handler.exceptionCaught(ctx, new RuntimeException())
        then:
        1 * ctx.close()
    }
}
