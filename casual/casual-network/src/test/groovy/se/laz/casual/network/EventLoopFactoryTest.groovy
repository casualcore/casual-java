/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network

import se.laz.casual.config.ConfigurationOptions
import se.laz.casual.config.ConfigurationService
import spock.lang.Specification

class EventLoopFactoryTest extends Specification
{

    def cleanup()
    {
        ConfigurationService.reload(  )
    }

    def 'correct instances are returned'()
    {
        given:
        def outboundEventLoopGroup
        def reverseEventLoopGroup

        when:
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_OUTBOUND_UNMANAGED, true ) //TODO: why two locations!

        outboundEventLoopGroup = EventLoopFactory.getInstance( EventLoopClient.OUTBOUND )
        reverseEventLoopGroup = EventLoopFactory.getInstance( EventLoopClient.REVERSE )

        then:
        outboundEventLoopGroup != reverseEventLoopGroup
        outboundEventLoopGroup == EventLoopFactory.getInstance( EventLoopClient.OUTBOUND )
        reverseEventLoopGroup == EventLoopFactory.getInstance( EventLoopClient.REVERSE )
    }
}
