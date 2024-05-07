/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.client


import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import spock.lang.Shared
import spock.lang.Specification

class EventClientInformationTest extends Specification
{
    @Shared
    ConnectionInformation actualConnectionInformation = new ConnectionInformation("localhost", 3789)
    @Shared
    def actualChannelClass = NioSocketChannel.class
    @Shared
    def actualEventLoop = new NioEventLoopGroup()

    def 'failed construction'()
    {
        when:
        EventClientInformation.createBuilder()
                .withChannelClass(channelClass)
                .withEventLoopGroup(eventLoop)
                .withConnectionInformation(connectionInformation)
                .build()
        then:
        thrown(NullPointerException)
        where:
        connectionInformation          || channelClass                   || eventLoop
        null                           || actualChannelClass             || actualEventLoop
        actualConnectionInformation    || null                           || actualEventLoop
        actualConnectionInformation    || actualChannelClass             || null
    }
}
