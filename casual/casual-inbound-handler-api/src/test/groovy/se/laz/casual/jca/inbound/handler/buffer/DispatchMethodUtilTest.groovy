package se.laz.casual.jca.inbound.handler.buffer

import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer
import se.laz.casual.jca.inbound.handler.InboundRequest
import se.laz.casual.jca.inbound.handler.test.TestService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Method

class DispatchMethodUtilTest extends Specification
{
    @Shared Method echoRequest = TestService.getMethod( "echo", InboundRequest.class )
    @Shared Method echoBuffer = TestService.getMethod( "echo", CasualBuffer.class )
    @Shared Method echoFieldedBuffer = TestService.getMethod( "echo", FieldedTypeBuffer.class )
    @Shared Method echoObject = TestService.getMethod( "echo", String.class )
    @Shared CasualBuffer buffer = FieldedTypeBuffer.create()
    @Shared InboundRequest request = InboundRequest.of( "echo", buffer )
    @Shared String message = "hi"

    @Unroll
    def "method accepts request"()
    {
        expect:
        DispatchMethodUtil.methodAccepts( method, request ) == result

        where:
        method     || result
        echoRequest|| true
        echoBuffer || false
        echoFieldedBuffer || false
        echoObject || false
    }

    @Unroll
    def "method accepts buffer"()
    {
        expect:
        DispatchMethodUtil.methodAccepts( method, buffer ) == result

        where:
        method     || result
        echoRequest|| false
        echoBuffer || true
        echoFieldedBuffer || true
        echoObject || false
    }

    @Unroll
    def "cast to method param"()
    {
        given:
        Object param = input

        when:
        Object[] actual = DispatchMethodUtil.toMethodParams( method, param )

        then:
        actual.length == 1
        actual[0].getClass() == expected

        where:
        method | input | expected
        echoRequest | request | InboundRequest.class
        echoFieldedBuffer | request.getBuffer() | FieldedTypeBuffer.class
    }

}
