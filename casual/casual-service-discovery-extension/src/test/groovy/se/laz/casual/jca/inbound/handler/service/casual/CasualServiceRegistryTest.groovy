/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual

import se.laz.casual.api.service.CasualService
import se.laz.casual.api.service.CasualServiceJndiName
import se.laz.casual.info.CasualInfo
import se.laz.casual.info.CasualInfoStorage
import se.laz.casual.info.Order
import se.laz.casual.info.ServiceDescriptor
import spock.lang.Specification

import java.lang.reflect.Method

class CasualServiceRegistryTest extends Specification
{
    def "register adds inbound service to CasualInfo"()
    {
        Method method = TestService.getMethod( "test", String.class );
        CasualService service = method.getAnnotation(CasualService.class)
        CasualServiceMetaData metadata = CasualServiceMetaData.newBuilder()
                .service(service)
                .serviceMethod(method)
                .implementationClass(TestService.class)
                .build();
        when:
        CasualServiceRegistry.getInstance().register(metadata);

        then:
        CasualInfoStorage.getInstance().getService( new ServiceDescriptor( metadata.serviceName, Order.SEQUENTIAL ) ).isPresent();
    }

    @CasualServiceJndiName("se.laz.casual.test.Service")
    private interface TestService
    {
        @CasualService(name="Test", category = "test" )
        String test( String message);
    }
}
