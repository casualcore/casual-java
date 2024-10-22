/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification

class ShutdownTest extends Specification
{
    long quiet = 150L
    long timeout = 500L

    Shutdown instance

    def setup()
    {
        instance = Shutdown.newBuilder().withQuietPeriod( quiet ).withTimeout( timeout ).build()
    }

    def "Create retrieve."()
    {
        expect:
        instance.getQuietPeriod(  ) == quiet
        instance.getTimeout(  ) == timeout
    }

    def "equals and hashcode checks"()
    {
        when:
        Shutdown instance2 = Shutdown.newBuilder( instance ).build()
        Shutdown instance3 = Shutdown.newBuilder( instance ).withTimeout( timeout +1  ).build()

        then:
        instance == instance
        instance2 == instance
        instance3 != instance
        instance.hashCode(  ) == instance.hashCode(  )
        instance2.hashCode(  ) == instance.hashCode(  )
        instance3.hashCode( ) != instance.hashCode(  )
        !instance.equals( "String" )
    }

    def "to string check"()
    {
        when:
        String actual = instance.toString(  )

        then:
        actual.contains( "" + quiet )
        actual.contains( "" + timeout )
    }

    def "Json roundtrip check."()
    {
        given:
        String json = JsonProviderFactory.getJsonProvider(  ).toJson( instance )

        when:
        Shutdown actual = JsonProviderFactory.getJsonProvider(  ).fromJson( json, Shutdown.class )

        then:
        actual == instance
    }

}
