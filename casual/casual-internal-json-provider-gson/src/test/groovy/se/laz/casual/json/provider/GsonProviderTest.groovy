/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.json.provider

import com.google.gson.Gson
import spock.lang.Shared
import spock.lang.Specification

class GsonProviderTest extends Specification
{
    @Shared String dataValue = "my value:" + UUID.randomUUID(  ).toString(  )

    def "Create object with default no arg constructor"()
    {
        given:
        DefaultNoArgCtor obj = new DefaultNoArgCtor()
        obj.setValue( dataValue )

        when:
        DefaultNoArgCtor actual = roundTrip( obj, DefaultNoArgCtor.class )

        then:
        actual == obj
    }

    def "Create object with no arg constructor."()
    {
        given:
        WithExplicitNoArgCtor obj = new WithExplicitNoArgCtor( )
        obj.setValue( dataValue )

        when:
        WithExplicitNoArgCtor actual = roundTrip( obj, WithExplicitNoArgCtor.class )

        then:
        actual == obj
    }

    def "Create object without no arg constructor."()
    {
        given:
        WithoutDefaultCtor obj = new WithoutDefaultCtor( dataValue )

        when:
        WithoutDefaultCtor actual = roundTrip( obj, WithoutDefaultCtor.class )

        then:
        actual == obj
    }

    def "Create object with multiple constructors including a no arg."()
    {
        given:
        MultipleCtorWithNoArg obj = new MultipleCtorWithNoArg()
        obj.setValue( dataValue )

        when:
        MultipleCtorWithNoArg actual = roundTrip( obj, MultipleCtorWithNoArg.class )

        then:
        actual == obj
    }

    def "Create object with multiple constructors but without a no arg."()
    {
        given:
        MultipleCtorWithoutDefault obj = new MultipleCtorWithoutDefault( dataValue )

        when:
        MultipleCtorWithoutDefault actual = roundTrip( obj, MultipleCtorWithoutDefault.class )

        then:
        actual == obj
    }

    def "Create object with a builder."()
    {
        given:
        WithBuilder obj = WithBuilder.newBuilder().withValue( dataValue ).build()

        when:
        WithBuilder actual = roundTrip( obj, WithBuilder.class )

        then:
        actual == obj
    }

    private <T> T roundTrip( T object, Class<T> klass )
    {
        Gson gson = new Gson()
        String json = gson.toJson( object )
        assert json.contains( dataValue )
        return gson.fromJson( json, klass )
    }

}
