/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import spock.lang.Specification

class ConfigurationOptionTest extends Specification
{

    String name = "mykey"
    ConfigurationOption<String> instance

    def setup()
    {
        instance = new ConfigurationOption<>( name )
    }

    def "Get value"()
    {
        when:
        ConfigurationOption<String> instance = new ConfigurationOption<>( name )

        then:
        instance.getName() == name
    }

    def "No value throws NullPointerExcepiton"()
    {
        when:
        new ConfigurationOption<Object>( null )

        then:
        thrown NullPointerException
    }

    def "equals and hashcode"()
    {
        when:
        ConfigurationOption<String> instance2 = new ConfigurationOption<>( "mykey" )
        ConfigurationOption<Integer> instance3 = new ConfigurationOption<>( "mykey1" )

        then:
        instance == instance
        instance2 == instance
        instance != instance3
        instance.hashCode(  ) == instance.hashCode(  )
        instance.hashCode(  ) == instance2.hashCode(  )
        instance.hashCode(  ) != instance3.hashCode(  )
        !instance.equals( "String" )
    }

    def "to string"()
    {
        when:
        String actual = instance.toString(  )

        then:
        actual.contains( name )
    }

}
