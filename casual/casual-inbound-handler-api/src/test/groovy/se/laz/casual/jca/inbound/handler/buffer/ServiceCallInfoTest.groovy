/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
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
    @Shared Object[] objects

    def setup()
    {
        method = String.class.getMethod( "split", String.class )
        objects = { "param" }
        instance = ServiceCallInfo.of( method, objects )
    }

    def "get method"()
    {
        expect:
        instance.getMethod().get() == method
    }

    def "get method null, return empty optional"()
    {
        setup:
        instance = ServiceCallInfo.of( objects )

        expect:
        ! instance.getMethod().isPresent()
    }

    def "get params"()
    {
        expect:
        instance.getParams() == objects
    }
}
