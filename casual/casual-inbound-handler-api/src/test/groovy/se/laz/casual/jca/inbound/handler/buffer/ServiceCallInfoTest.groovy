/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer

import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method

class ServiceCallInfoTest extends Specification
{
    @Shared ServiceCallInfo instance
    @Shared Method method
    @Shared Method realMethod
    @Shared Object[] objects

    def setup()
    {
        realMethod = String.class.getMethod( "startsWith", String.class )
        method = String.class.getMethod( "split", String.class )
        objects = { "param" }
        instance = ServiceCallInfo.of( method, realMethod, objects )
    }

    def "get method"()
    {
        expect:
        instance.getMethod().get() == method
    }

    def "get real method"()
    {
       expect:
       instance.getRealMethod().get() == realMethod
    }

    def "get methods null, return empty optional"()
    {
        setup:
        instance = ServiceCallInfo.of( objects )

        expect:
        ! instance.getMethod().isPresent()
        ! instance.getRealMethod().isPresent()
    }

    def "get params"()
    {
        expect:
        instance.getParams() == objects
    }
}
