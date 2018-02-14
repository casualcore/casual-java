package se.kodarkatten.casual.jca.inbound.handler.buffer

import se.kodarkatten.casual.jca.inbound.handler.test.TestBufferHandler
import se.kodarkatten.casual.jca.inbound.handler.test.TestBufferHandler2
import spock.lang.Specification
import spock.lang.Unroll

class BufferHandlerFactoryTest extends Specification
{

    def "GetHandlers"()
    {
        when:
        List<BufferHandler> actual = BufferHandlerFactory.getHandlers()

        then:
        actual.size() == 2
    }

    @Unroll
    def "GetHandler with a buffer type returns the handler that responses for that buffer type."()
    {
        when:
        BufferHandler h = BufferHandlerFactory.getHandler( serviceName )
        BufferHandler h2 = BufferHandlerFactory.getHandler( serviceName )

        then:
        h.getClass() == handlerType
        h2.getClass() == handlerType
        h == h2

        where:
        serviceName                     | handlerType
        TestBufferHandler.BUFFER_TYPE_1 | TestBufferHandler.class
        TestBufferHandler2.BUFFER_TYPE_2| TestBufferHandler2.class
    }

    def "GetHandler buffer type unknown, returns passthrough handler."()
    {
        when:
        BufferHandler h = BufferHandlerFactory.getHandler( "unknownn" )

        then:
        h.getClass() == PassThroughBufferHandler.class
    }
}
