/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification

class ReverseOutboundTest extends Specification
{
    String name = "reverse-pool"
    int port = 7894

    ReverseOutbound instance

    def setup()
    {
        instance = ReverseOutbound.newBuilder().withName( name ).withPort( port ).build()
    }

    def 'sanity'()
    {
        expect:
        instance.getName(  ) == name
        instance.getPort(  ) == port
    }

    def 'equals and hashcode'()
    {
        when:
        ReverseOutbound instanceTwo = ReverseOutbound.newBuilder( instance ).build()
        ReverseOutbound instanceThree = ReverseOutbound.newBuilder( instance ).withPort( port +1 ).build()

        then:
        instance == instance
        instanceTwo == instance
        instanceThree != instance
        instance.hashCode(  ) == instance.hashCode(  )
        instanceTwo.hashCode(  ) == instance.hashCode(  )
        instanceThree.hashCode( ) != instance.hashCode(  )
    }

    def 'to String'()
    {
        when:
        String actual = instance.toString()

        then:
        actual.contains( name )
        actual.contains( "" + port )
    }

    def 'Json roundtrip'()
    {
        given:
        String json = JsonProviderFactory.getJsonProvider(  ).toJson( instance )

        when:
        ReverseOutbound actual = JsonProviderFactory.getJsonProvider(  ).fromJson( json, ReverseOutbound.class )

        then:
        actual == instance
    }
}
