/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer

import spock.lang.Specification

class CasualHeadersTest extends Specification
{
    CasualHeaders instance

    def setup()
    {
        instance = CasualHeaders.newBuilder()
                .add("a", "foo" )
                .add("b", "bar" )
                .add("c", "baz" )
                .add("c", "baa" )
                .build()
    }

    def "Create then get."()
    {
        expect:
        instance.get( "a" ) == ["foo"]
        instance.get( "b" ) == ["bar"]
        instance.get( "c" ) == ["baz","baa"]
        instance.size() == 4
    }

    def "New / empty size is 0."()
    {
        when:
        instance = CasualHeaders.newBuilder().build(  )

        then:
        instance.size(  ) == 0
    }

    def "Empty static instances is equal to a new empty instance."()
    {
        when:
        CasualHeaders instance1 = CasualHeaders.empty()
        CasualHeaders instance2 = CasualHeaders.newBuilder().build(  )

        then:
        instance1.equals( instance2 )
        !instance.equals( instance1 )
        instance1.hashCode(  ) == instance2.hashCode(  )
        instance.hashCode(  ) != instance1.hashCode(  )
    }

    def "Get headers does not exist, returns empty."()
    {
        when:
        List<String> actual = instance.get( "d" )

        then:
        actual == Collections.emptyList(  )
    }

    def "Get with null throws NullPointerException."()
    {
        when:
        instance.get( null )

        then:
        thrown NullPointerException
    }

    def "Add header invalid."()
    {
        when:
        CasualHeaders.newBuilder()
                .add( name, value )

        then:
        thrown NullPointerException

        where:
        name    | value
        null    | "123"
        "value" | null
    }

    def "Check if header is contained."()
    {
        when:
        boolean actual = instance.containsName( name )

        then:
        actual == expected

        where:
        name | expected
        "a" | true
        "b" | true
        "c" | true
        "d" | false
    }

    def "Get header names"()
    {
        given:
        Set<String> expected = ["a","b","c" ]

        when:
        Set<String> actual = instance.getNames( )

        then:
        actual == expected
    }

    def "Equals and hashcode."()
    {
        when:
        CasualHeaders instance2 = CasualHeaders.newBuilder( instance ).build()
        CasualHeaders instance3 = CasualHeaders.newBuilder( instance ).add( "other","something").build()

        then:
        instance == instance
        instance.hashCode(  ) == instance.hashCode(  )
        instance2 == instance
        instance2.hashCode(  ) == instance.hashCode(  )
        instance3 == instance3
        instance3.hashCode(  ) == instance3.hashCode(  )
        instance3 != instance
        instance3.hashCode(  ) != instance.hashCode(  )

        !instance.equals( "String" )
    }

    def "to string"()
    {
        when:
        String actual = instance.toString()

        then:
        actual.contains( "a" )
        actual.contains( "b" )
        actual.contains( "c" )
        actual.contains( "foo" )
        actual.contains( "bar" )
        actual.contains( "baz" )
        actual.contains( "baa" )
    }

    def "Add fully."()
    {
        when:
        CasualHeaders instance2 = CasualHeaders.newBuilder()
                .add( "a:foo" )
                .add( "b:bar" )
                .add( "c:baz" )
                .add( "c:baa" ).build()

        then:
        instance2 == instance
    }

    def "Add fully without right format invalid."()
    {
        when:
        CasualHeaders.newBuilder( ).add( "me" )

        then:
        thrown IllegalArgumentException
    }

    def "Add same header multiple times, is ok."()
    {
        when:
        instance = CasualHeaders.newBuilder().add( "me","here" ).add( "me","here" ).build()
        List<String> actual = instance.get( "me" )

        then:
        instance.size() == 2
        actual.size() == 2
        actual == ["here","here"]
    }

    def "Add all headers from a list of colon delimited strings."()
    {
        List<String> all = ["a:foo", "b:bar", "c:baz", "c:baa" ]

        when:
        CasualHeaders instance2 = CasualHeaders.newBuilder().addAll( all ).build()

        then:
        instance2 == instance
    }

    def "Add all headers from a list of colon delimited strings, one invalid, throws IllegalArgumentException."()
    {
        List<String> all = ["a:foo", "b:bar", "cbaz", "c:baa" ]

        when:
        CasualHeaders.newBuilder().addAll( all )

        then:
        thrown IllegalArgumentException
    }

    def "Get all header entries."()
    {
        List<String> expected = [
                "a:foo",
                "b:bar",
                "c:baz",
                "c:baa"
        ]
        when:
        List<String> actual = instance.getAll( )

        then:
        actual == expected
    }

}
