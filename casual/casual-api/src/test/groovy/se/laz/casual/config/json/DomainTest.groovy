/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification

class DomainTest extends Specification
{
    Domain instance
    UUID id = UUID.randomUUID(  )
    String name = "test-domain"

    def setup()
    {
        instance = Domain.newBuilder().id( id ).name( name ).build()
    }

    def "Create retrieve."()
    {
        expect:
        instance.getId() == id
        instance.getName() == name
    }

    def "equals and hashcode"()
    {
        when:
        Domain instance2 = Domain.newBuilder( instance ).build()
        Domain instance3 = Domain.newBuilder( instance ).id( UUID.randomUUID(  ) ).build()

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
        actual.contains( id.toString(  ) )
        actual.contains( name )
    }

    def "Json roundtrip check."()
    {
        given:
        String json = JsonProviderFactory.getJsonProvider(  ).toJson( instance )

        when:
        Domain actual = JsonProviderFactory.getJsonProvider(  ).fromJson( json, Domain.class )

        then:
        actual == instance
    }
}
