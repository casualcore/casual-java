/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded

import se.laz.casual.api.Utils.ResourceLoader
import se.laz.casual.api.buffer.CasualHeaders
import spock.lang.Shared
import spock.lang.Specification

import static se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer.create
import static se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer.createAllowNullUseDefault

class FieldedTypeBufferTest extends Specification
{

    @Shared String resource = '/fielded/field.bin'
    @Shared byte[] data
    @Shared List<String> rawHeaders = ["a:foo","b:bar", "c:baz" ]
    @Shared CasualHeaders h


    def setupSpec()
    {
        h = CasualHeaders.newBuilder().addAll( rawHeaders ).build(  )
        data = ResourceLoader.getResourceAsByteArray(resource)
        then:
        data != null
        data.length == 154
    }

    def "Create without headers - empty."()
    {
        when:
        CasualHeaders actual = buffer.getHeaders()

        then:
        actual == CasualHeaders.empty(  )

        where:
        buffer << [
                create(),
                create( [data]),
                createAllowNullUseDefault(),
                createAllowNullUseDefault( [data] ),
                FieldedTypeBuffer.of(  create() ),
                FieldedTypeBuffer.ofAllowNullUseDefault(  create() )
        ]
    }

    def "Create with headers."()
    {
        when:
        CasualHeaders actual = buffer.getHeaders()

        then:
        actual == expected

        where:
        buffer                                                                         | expected
        create( h )                                            | h
        create( [data], h )                                    | h
        createAllowNullUseDefault( h )                         | h
        createAllowNullUseDefault( [data], h )                 | h
        FieldedTypeBuffer.of( create( h ) )                    | h
        FieldedTypeBuffer.ofAllowNullUseDefault( create( h ) ) | h
    }

    def "Create with null headers, throws NullPointerException"()
    {

        given:
        CasualHeaders header = null

        when:
        create( header )

        then:
        thrown NullPointerException

        when:
        create( [data], header )

        then:
        thrown NullPointerException

        when:
        createAllowNullUseDefault( header as CasualHeaders )

        then:
        thrown NullPointerException

        when:
        createAllowNullUseDefault( [data], header )

        then:
        thrown NullPointerException

    }

    def "equals and hashcode"()
    {
        when:
        FieldedTypeBuffer instance5 = FieldedTypeBuffer.of( instance1 )
        FieldedTypeBuffer instance6 = FieldedTypeBuffer.ofAllowNullUseDefault( instance1 )

        then:
        instance1 == instance1
        instance2 == instance1
        instance5 == instance1
        instance6== instance1
        instance3 != instance1
        instance1.hashCode(  ) == instance1.hashCode(  )
        instance1.hashCode() == instance2.hashCode()
        instance1.hashCode() == instance5.hashCode()
        instance1.hashCode() == instance6.hashCode()
        instance3.hashCode() != instance1.hashCode()
        !instance1.equals( "String" )

        where:
        instance1                           | instance2                           | instance3
        create()                            | create()                            | create( [data] )
        create()                            | create()                            | create( h )
        create( [data] )                    | create( [data] )                    | create( [data], h )
        create( h )                         | create( h )                         | create( [data], h )
        createAllowNullUseDefault()         | createAllowNullUseDefault()         | createAllowNullUseDefault( [data] )
        createAllowNullUseDefault( h )      | createAllowNullUseDefault( h )      | createAllowNullUseDefault()
        createAllowNullUseDefault( [data] ) | createAllowNullUseDefault( [data] ) | createAllowNullUseDefault([data],h)
        createAllowNullUseDefault( [data] ) | createAllowNullUseDefault( [data] ) | createAllowNullUseDefault([data],h)

    }
}
