/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual

import se.laz.casual.api.service.CasualService
import se.laz.casual.api.service.CasualServiceJndiName
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method

class CasualServiceMetaDataTest extends Specification
{

    @Shared CasualServiceMetaData instance
    @Shared CasualService service
    @Shared String appName = "test-app"
    @Shared String moduleName = "test-module"
    @Shared Class<?> serviceClass = Object.class
    @Shared Class<?> interfaceClass = String.class
    @Shared String ejbName = "ejb name"
    @Shared CasualServiceJndiName serviceJndiName
    @Shared String jndiName = "jndi name"
    @Shared String serviceName = "service name"
    @Shared String category = "some category"
    @Shared Method method = String.class.getMethod( "toString" )
    @Shared CasualServiceMetaData metaData = CasualServiceMetaData.newBuilder()
            .service(Mock(CasualService))
            .implementationClass(String.class)
            .serviceMethod(Object.class.getMethods()[0])
            .build()


    def setup()
    {
        service = new CasualServiceLiteral( serviceName, category )
        serviceJndiName = new CasualServiceJndiNameLiteral( jndiName )

        instance = CasualServiceMetaData.newBuilder()
                .service( service )
                .appName( appName )
                .moduleName( moduleName )
                .implementationClass( serviceClass )
                .interfaceClass( interfaceClass )
                .ejbName( ejbName )
                .serviceJndiName( serviceJndiName )
                .serviceMethod( method )
                .build()
    }

    def "Get Service name"()
    {
        expect:
        instance.getServiceName( ) == serviceName
    }

    def "Get category"()
    {
        expect:
        instance.getServiceCategory() == category
    }

    def "Get application name"()
    {
        expect:
        instance.getAppName().get() == appName
    }

    def "Get module name"()
    {
        expect:
        instance.getModuleName().get() == moduleName
    }

    def "Get ejb name"()
    {
        expect:
        instance.getEjbName().get() == ejbName
    }

    def "Get service jndi name"()
    {
        expect:
        instance.getJndiName().get() == jndiName
    }

    def "Get service method."()
    {
        expect:
        instance.getServiceMethod() == method
    }

    def "is unresolved"()
    {
        expect:
        instance.isUnresolved()
    }

    def "Set resolved entry."()
    {
        given:
        instance.setResolvedEntry( CasualServiceEntry.of( serviceName, jndiName, method, metaData ) )

        expect:
        ! instance.isUnresolved()
    }
}
