/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.store

import spock.lang.Specification

class AbstractResourceStoreTest extends Specification
{

    AbstractResourceStore<String> instance = new AbstractResourceStore<String>() {}

    def "Put and retrieve by name, retrieved."()
    {
        given:
        String name = "name"
        String value = "value"
        instance.put( name, value )

        when:
        String actual = instance.get( name )

        then:
        actual == value
    }

    def "Get by name, missing throws ResourceNotFoundException."()
    {
        when:
        instance.get( "blah" )

        then:
        thrown ResourceNotFoundException
    }

    def "Get by name, null, throws NullPointerException."()
    {
        when:
        instance.get( null )

        then:
        thrown NullPointerException
    }

    def "Put and retrieve all, retrieved."()
    {
        given:
        instance.put( "name", "value")
        instance.put( "name2", "value2" )

        when:
        Map<String,String> actual = instance.getAll(  )

        then:
        actual == ["name": "value", "name2": "value2" ]
    }

    def "Put then updated and retrieve."()
    {
        given:
        instance.put( "name", "value" )
        instance.put( "name2", "value2" )

        when:
        instance.put( "name", "valueupdated" )
        String actual = instance.get( "name" )

        then:
        actual == "valueupdated"
        instance.getAll(  ) == ["name": "valueupdated", "name2": "value2" ]
    }

    def "Put with nulls, throws NullPointerException"()
    {
        when:
        instance.put( name, value )

        then:
        thrown NullPointerException

        where:
        name   | value
        null   | "value"
        "name" | null
        null   | null
    }

    def "Put then Remove."()
    {
        given:
        String name = "name"
        String value = "value"
        instance.put( name, value )
        instance.put( "name2", "value2" )

        when:
        String actual = instance.remove( name )

        then:
        actual == value
        instance.getAll(  ) == ["name2": "value2" ]
    }

    def "Put all."()
    {
        given:
        Map<String,String> all = ["name":"value", "name2": "value2" ]
        instance.put( "other","value")

        when:
        instance.putAll( all )

        then:
        instance.getAll(  ) == ["name":"value", "name2": "value2", "other": "value" ]
    }

    def "Put all, null, throws NullPointerException."()
    {
        when:
        instance.putAll( null )

        then:
        thrown NullPointerException
    }

    def "Remove not stored, throws ResourceNotFoundException."()
    {
        when:
        instance.remove( "blah" )

        then:
        thrown ResourceNotFoundException
    }

    def "Remove null name, throws NullPointerException."()
    {
        when:
        instance.remove( null )

        then:
        thrown NullPointerException
    }

}
