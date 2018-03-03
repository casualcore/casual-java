/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service

import se.laz.casual.jca.inbound.handler.test.TestHandler
import se.laz.casual.jca.inbound.handler.test.TestHandler2
import spock.lang.Specification
import spock.lang.Unroll

class ServiceHandlerFactoryTest extends Specification
{

    def "GetHandlers"()
    {
        when:
        List<ServiceHandler> actual = ServiceHandlerFactory.getHandlers()

        then:
        actual.size() == 2
    }

    @Unroll
    def "GetHandler with a service name returns the handler that responses for that service."()
    {
        when:
        ServiceHandler h = ServiceHandlerFactory.getHandler( serviceName )
        ServiceHandler h2 = ServiceHandlerFactory.getHandler( serviceName )

        then:
        h.getClass() == handlerType
        h2.getClass() == handlerType
        h == h2

        where:
        serviceName            | handlerType
        TestHandler.SERVICE_1  | TestHandler.class
        TestHandler2.SERVICE_2 | TestHandler2.class
    }

    def "GetHandler service name not handled, throws CasualHandlerException."()
    {
        when:
        ServiceHandlerFactory.getHandler( "unknownn" )

        then:
        thrown ServiceHandlerNotFoundException.class
    }
}
