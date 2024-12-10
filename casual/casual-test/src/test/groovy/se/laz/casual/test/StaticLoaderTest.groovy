/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test

import spock.lang.Specification

import java.lang.reflect.Method

class StaticLoaderTest extends Specification
{
    def cleanup()
    {
        StaticData.set( 0 )
    }

    def "Retrieve an instance of static data."()
    {
        when:
        StaticData.increment(  )

        then:
        StaticData.get(  ) == 1
    }

    def "Retrieve via loader StaticData, invoke using reflection."()
    {
        given:
        Class<StaticData> data = StaticLoader.getInstance()
        Method increment = data.getMethod( "increment" )
        Method get = data.getMethod( "get" )

        when:
        increment.invoke( null )

        then:
        get.invoke( null ) == 1
        StaticData.get( ) == 1
    }

    def "Create via loader StaticData, invoke using reflection."()
    {
        given:
        Class<StaticData> data = StaticLoader.createInstance()
        Method increment = data.getMethod( "increment" )
        Method get = data.getMethod( "get" )

        when:
        increment.invoke( null )

        then:
        get.invoke( null ) == 1
        StaticData.get( ) == 1
    }

    def "Create via loader StaticData, invoke using reflection."()
    {
        given:
        Class<StaticData> data = StaticLoader.createNewInstance()
        Method increment = data.getMethod( "increment" )
        Method get = data.getMethod( "get" )

        when:
        increment.invoke( null )

        then:
        get.invoke( null ) == 1
        StaticData.get( ) == 0
    }

    def "Create via loader 2 StaticData, invoke using reflection."()
    {
        given:
        Class<StaticData> data = StaticLoader.createNewInstance()
        Method increment = data.getMethod( "increment" )
        Method get = data.getMethod( "get" )

        Class<StaticData> data2 = StaticLoader.createNewInstance()
        Method increment2 = data2.getMethod( "increment" )
        Method get2 = data2.getMethod( "get" )

        when:
        increment.invoke( null )

        then:
        get.invoke( null ) == 1
        get2.invoke( null ) == 0
        StaticData.get( ) == 0

        when:
        increment.invoke( null )
        increment2.invoke( null )

        then:
        get.invoke( null ) == 2
        get2.invoke( null ) == 1
        StaticData.get( ) == 0
    }

}
