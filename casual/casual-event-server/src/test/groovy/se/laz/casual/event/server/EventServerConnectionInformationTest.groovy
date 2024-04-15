/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.server

import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import spock.lang.Specification

class EventServerConnectionInformationTest extends Specification
{
    boolean useEpoll = true
    int port = 4567
    long timeout = 3000
    long quiet = 2000

    EventServerConnectionInformation instance

    def setup()
    {
        instance = EventServerConnectionInformation.createBuilder(  )
            .withPort( port )
            .withUseEpoll( useEpoll )
            .withShutdownTimeout( timeout )
            .withShutdownQuietPeriod( quiet )
            .build()
    }

    def "Create then get."()
    {
        expect:
        instance.getPort(  ) == port
        instance.isUseEpoll(  ) == useEpoll
        instance.getShutdownTimeout( ) == timeout
        instance.getShutdownQuietPeriod( ) == quiet
    }

    def "Equals and hashcode"()
    {
        when:
        EventServerConnectionInformation instance2 = EventServerConnectionInformation.createBuilder( instance ).build()
        EventServerConnectionInformation instance3 = EventServerConnectionInformation.createBuilder( instance )
                .withUseEpoll( !useEpoll )
                .build(  )

        then:
        instance == instance
        instance == instance2
        instance != instance3
        instance.hashCode(  ) == instance.hashCode(  )
        instance2.hashCode(  ) == instance.hashCode(  )
        instance3.hashCode(  ) != instance.hashCode(  )
        !instance.equals( "String")
    }

    def "to string"()
    {
        when:
        String actual = instance.toString(  )

        then:
        actual.contains( ""+useEpoll )
        actual.contains( ""+port )
        actual.contains( ""+quiet )
        actual.contains( ""+timeout )
    }

    def "channel class dependant on epoll."()
    {
        given:
        EventServerConnectionInformation info = EventServerConnectionInformation.createBuilder( instance )
                .withUseEpoll( epoll )
                .build(  )

        when:
        Class<?> actual = info.getChannelClass(  )

        then:
        actual == expected

        where:
        epoll || expected
        true  || EpollServerSocketChannel.class
        false || NioServerSocketChannel.class
    }
}
