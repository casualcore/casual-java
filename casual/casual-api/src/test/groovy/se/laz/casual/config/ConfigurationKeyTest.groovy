/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import spock.lang.Specification

class ConfigurationKeyTest extends Specification
{

    def "Get value"()
    {
        given:
        String keyValue = "mykey"

        when:
        ConfigurationKey<String> instance = new ConfigurationKey<>( keyValue )

        then:
        instance.getValue() == keyValue
    }

    def "No value throws NullPointerExcepiton"()
    {
        when:
        new ConfigurationKey<Object>( null )

        then:
        thrown NullPointerException
    }

    def "equals and hashcode"()
    {
        when:
        ConfigurationKey<String> instance = new ConfigurationKey<>( "mykey" )
        ConfigurationKey<String> instance2 = new ConfigurationKey<>( "mykey" )
        ConfigurationKey<Integer> instance3 = new ConfigurationKey<>( "mykey1" )

        then:
        instance == instance
        instance2 == instance
        instance != instance3
        instance.hashCode(  ) == instance.hashCode(  )
        instance.hashCode(  ) == instance2.hashCode(  )
        instance.hashCode(  ) != instance3.hashCode(  )
        !instance.equals( "String" )
    }


}
