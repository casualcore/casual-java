package se.kodarkatten.casual.jca.inbound.handler

import se.kodarkatten.casual.jca.inbound.handler.test.TestHandler
import se.kodarkatten.casual.jca.inbound.handler.test.TestHandler2
import spock.lang.Specification
import spock.lang.Unroll

class CasualHandlerFactoryTest extends Specification
{

    def "GetHandlers"()
    {
        when:
        List<CasualHandler> actual = CasualHandlerFactory.getHandlers()

        then:
        actual.size() == 2
    }

    @Unroll
    def "GetHandler with a service name returns the handler that responses for that service."()
    {
        when:
        CasualHandler h = CasualHandlerFactory.getHandler( serviceName )
        CasualHandler h2 = CasualHandlerFactory.getHandler( serviceName )

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
        CasualHandlerFactory.getHandler( "unknownn" )

        then:
        thrown CasualHandlerException.class
    }
}
