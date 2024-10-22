/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification

class EventServerTest extends Specification
{
    boolean epoll = true
    int port = 7894
    Shutdown shutdown = Shutdown.newBuilder().build()

    EventServer instance

    def setup()
    {
        instance = EventServer.newBuilder().withUseEpoll( epoll ).withPort( port ).withShutdown( shutdown ).build()
    }

    def "Create then Retrieve."()
    {
        expect:
        instance.getUseEpoll(  ) == epoll
        instance.getPortNumber(  ) == port
        instance.getShutdown(  ) == shutdown
    }

    def "equals and hashcode."()
    {
        when:
        EventServer instance2 = EventServer.newBuilder( instance ).build()
        EventServer instance3 = EventServer.newBuilder( instance ).withPort( port +1 ).build()

        then:
        instance == instance
        instance2 == instance
        instance3 != instance
        instance.hashCode(  ) == instance.hashCode(  )
        instance2.hashCode(  ) == instance.hashCode(  )
        instance3.hashCode( ) != instance.hashCode(  )
        !instance.equals( "String" )
    }

    def "to String check"()
    {
        when:
        String actual = instance.toString()

        then:
        actual.contains( "" + epoll )
        actual.contains( "" + port )
        actual.contains( shutdown.toString(  ) )
    }

    def "Json roundtrip check."()
    {
        given:
        String json = JsonProviderFactory.getJsonProvider(  ).toJson( instance )

        when:
        EventServer actual = JsonProviderFactory.getJsonProvider(  ).fromJson( json, EventServer.class )

        then:
        actual == instance
    }

}
