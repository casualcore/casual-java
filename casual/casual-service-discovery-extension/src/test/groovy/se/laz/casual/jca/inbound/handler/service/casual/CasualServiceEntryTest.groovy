/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual

import se.laz.casual.api.service.CasualService
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method

class CasualServiceEntryTest extends Specification
{

    @Shared CasualServiceEntry instance
    @Shared String name = "test-service-name"
    @Shared String jndi = "java:global/app/ejb!interface"
    @Shared Method method = String.class.getMethod( "toString" )
    @Shared CasualServiceMetaData metaData = CasualServiceMetaData.newBuilder()
            .service(Mock(CasualService))
            .implementationClass(String.class)
            .serviceMethod(Object.class.getMethods()[0])
            .build()

    def setup()
    {
        instance = CasualServiceEntry.of( name, jndi, method, metaData )
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

   def "Get metaData"()
   {
      expect:
      instance.getMetaData() == metaData
   }

}
