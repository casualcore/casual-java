/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification

class InboundTest extends Specification
{
    boolean epoll = true
    long delay = 123L
    Startup startup = Startup.newBuilder().build(  )

    Inbound instance

    def setup()
    {
        instance = Inbound.newBuilder(  ).withInitialDelay( delay ).withUseEpoll( epoll ).withStartup( startup ).build()
    }

    def "Create and retrieve"()
    {
        expect:
        instance.getInitialDelay(  ) == delay
        instance.getUseEpoll(  ) == epoll
        instance.getStartup( ) == startup
    }

    def "equals and hashcode checks."()
    {
        when:
        Inbound instance2 = Inbound.newBuilder( instance ).build()
        Inbound instance3 = Inbound.newBuilder( instance ).withInitialDelay( delay +1L ).build(  )

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
        actual.contains( "" + delay )
        actual.contains( startup.toString(  ) )
    }

    def "Json roundtrip check."()
    {
        given:
        String json = JsonProviderFactory.getJsonProvider(  ).toJson( instance )

        when:
        Inbound actual = JsonProviderFactory.getJsonProvider(  ).fromJson( json, Inbound.class )

        then:
        actual == instance
    }
}
