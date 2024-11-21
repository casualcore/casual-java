/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification

class AddressTest extends Specification
{
    String host = "127.0.0.1"
    int port = 7759

    Address instance

    def setup()
    {
        instance = Address.newBuilder().withHost( host ).withPort( port ).build()
    }

    def "Create then retrieve."()
    {
        expect:
        instance.getHost() == host
        instance.getPort() == port
    }

    def "equals and hashcode."()
    {
        when:
        Address instance2 = Address.newBuilder( instance ).build()
        Address instance3 = Address.newBuilder( instance ).withPort( port +1 ).build()

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
        actual.contains( host )
        actual.contains( "" + port )
    }

    def "Json roundtrip check."()
    {
        given:
        String json = JsonProviderFactory.getJsonProvider(  ).toJson( instance )

        when:
        Address actual = JsonProviderFactory.getJsonProvider(  ).fromJson( json, Address.class )

        then:
        actual == instance
    }
}
