package se.laz.casual.jca.inbound.handler.buffer

import se.laz.casual.api.CasualRuntimeException
import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer
import se.laz.casual.jca.inbound.handler.InboundRequest
import se.laz.casual.jca.inbound.handler.InboundResponse
import se.laz.casual.jca.inbound.handler.test.TestService
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method

class PassThroughBufferHandlerTest extends Specification
{

    @Shared BufferHandler instance
    @Shared Method echoRequest = TestService.getMethod( "echo", InboundRequest.class )
    @Shared Method echoBuffer = TestService.getMethod( "echo", FieldedTypeBuffer.class )
    @Shared Method echoString = TestService.getMethod( "echo", String.class )

    def setup()
    {
        instance = new PassThroughBufferHandler()
    }

    def "Can handle buffer all"()
    {
        given:
        String type = FieldedTypeBuffer.getTypeName()

        expect:
        instance.canHandleBuffer( type )
    }

    def "fromRequest with InboundRequest passes through"()
    {
        given:
        CasualBuffer buffer = FieldedTypeBuffer.create()
        InboundRequest request = InboundRequest.of( "test123", buffer )

        when:
        ServiceCallInfo actual = instance.fromRequest( null, echoRequest, request )

        then:
        actual != null
        actual.getMethod().get() == echoRequest
        actual.getParams().length == 1
        actual.getParams()[0] == request
    }

    def "fromRequest with InboundRequest with method that accepts buffer passes buffer through"()
    {
        given:
        CasualBuffer buffer = FieldedTypeBuffer.create()
        InboundRequest request = InboundRequest.of( "test123", buffer )

        when:
        ServiceCallInfo actual = instance.fromRequest( null, echoBuffer, request )

        then:
        actual != null
        actual.getMethod().get() == echoBuffer
        actual.getParams().length == 1
        actual.getParams()[0] == buffer
    }

    def "fromRequest with InboundRequest with method that accepts String throws CasualRuntimeException."()
    {
        given:
        CasualBuffer buffer = FieldedTypeBuffer.create()
        InboundRequest request = InboundRequest.of( "test123", buffer )

        when:
        ServiceCallInfo actual = instance.fromRequest( null, echoString, request )

        then:
        thrown CasualRuntimeException
    }

    def "toResponse with buffer wraps inside Response"()
    {
        given:
        CasualBuffer buffer = FieldedTypeBuffer.create()
        ServiceCallInfo info = Mock(ServiceCallInfo)

        when:
        InboundResponse response = instance.toResponse( info, buffer )

        then:
        response.getBuffer() == buffer
    }

    def "toResponse with Response returns response."()
    {
        given:
        CasualBuffer buffer = FieldedTypeBuffer.create()
        InboundResponse response = InboundResponse.createBuilder().buffer( buffer ).build()
        ServiceCallInfo info = Mock(ServiceCallInfo)

        when:
        InboundResponse actual = instance.toResponse( info, response )

        then:
        actual == response
    }

}
