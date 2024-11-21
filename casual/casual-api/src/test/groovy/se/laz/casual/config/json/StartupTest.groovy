/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification

class StartupTest extends Specification
{
    Mode mode = Mode.DISCOVER
    List<String> services = ["myservice","other-service"]

    Startup instance

    def setup()
    {
        instance = Startup.newBuilder().withMode( mode ).withServices( services ).build(  )
    }

    def "Create retrieve"()
    {
        expect:
        instance.getMode(  ) == mode
        instance.getServices(  ) == services
    }

    def "equals hashcode check"()
    {
        when:
        Startup instance2 = Startup.newBuilder( instance ).build()
        Startup instance3 = Startup.newBuilder(  ).withMode(Mode.IMMEDIATE  ).build(  )

        then:
        instance == instance
        instance2 == instance
        instance3 != instance
        instance.hashCode(  ) == instance.hashCode(  )
        instance2.hashCode(  ) == instance.hashCode(  )
        instance3.hashCode( ) != instance.hashCode(  )
        !instance.equals( "String" )
    }

    def "service ordering irrelevant with equality check."()
    {
        given:
        List<String> s1 = [ "s1", "s2", "s3" ]
        List<String> s2 = [ "s2", "s3", "s1" ]

        when:
        Startup instance2 = Startup.newBuilder(  ).withMode( Mode.DISCOVER ).withServices( s1 ).build(  )
        Startup instance3 = Startup.newBuilder(  ).withMode( Mode.DISCOVER ).withServices( s2 ).build(  )

        then:
        instance3 == instance2
        instance3.hashCode(  ) == instance2.hashCode(  )
        instance3.getServices(  ) == instance2.getServices(  )
    }

    def "to string check"()
    {
        when:
        String actual = instance.toString(  )

        then:
        actual.contains( mode.toString(  ) )
        actual.contains( services.toString(  ) )
    }

    def "Json roundtrip check."()
    {
        given:
        String json = JsonProviderFactory.getJsonProvider(  ).toJson( instance )

        when:
        Startup actual = JsonProviderFactory.getJsonProvider(  ).fromJson( json, Startup.class )

        then:
        actual == instance
    }

}
