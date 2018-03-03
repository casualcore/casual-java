/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual

import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method

class CasualServiceEntryTest extends Specification
{

    @Shared CasualServiceEntry instance
    @Shared String name = "test-service-name"
    @Shared String jndi = "java:global/app/ejb!interface"
    @Shared Method method = String.class.getMethod( "toString" )

    def setup()
    {
        instance = CasualServiceEntry.of( name, jndi, method )
    }

    def "Get service name"()
    {
        expect:
        instance.getServiceName() == name
    }

    def "Get jndi name"()
    {
        expect:
        instance.getJndiName() == jndi
    }

    def "Get method"()
    {
        expect:
        instance.getProxyMethod() == method
    }
}
