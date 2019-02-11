/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer.jscd


import se.laz.casual.api.buffer.CasualBufferType
import se.laz.casual.api.buffer.type.JavaServiceCallDefinition
import se.laz.casual.api.external.json.JsonProvider
import se.laz.casual.api.external.json.JsonProviderFactory
import se.laz.casual.jca.inbound.handler.HandlerException
import se.laz.casual.jca.inbound.handler.InboundRequest
import se.laz.casual.jca.inbound.handler.InboundResponse
import se.laz.casual.jca.inbound.handler.buffer.ServiceCallInfo
import se.laz.casual.api.buffer.type.ServiceBuffer
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Proxy
import java.nio.charset.StandardCharsets

class JavaServiceCallBufferHandlerTest extends Specification
{
    @Shared JavaServiceCallBufferHandler instance

    @Shared Proxy jndiObject

    @Shared JavaServiceCallDefinition serialisedCall

    @Shared String methodName = "echo"
    @Shared String methodParam = "method param1"
    @Shared SimpleService proxyService
    @Shared JsonProvider jp = JsonProviderFactory.getJsonProvider()
    @Shared String json
    @Shared List<byte[]> payload
    @Shared ServiceBuffer buffer

    def setup()
    {
        instance = new JavaServiceCallBufferHandler()

        serialisedCall = JavaServiceCallDefinition.of( methodName, methodParam )

        json = jp.toJson( serialisedCall )

        payload = new ArrayList<>()

        payload.add( json.getBytes( StandardCharsets.UTF_8 ) )

        buffer = ServiceBuffer.of(CasualBufferType.JSON_JSCD.getName(), payload )

        Class[] c = new Class[1]
        c[0] = SimpleService.class
        proxyService = Mock( SimpleService )

        jndiObject = (Proxy)Proxy.newProxyInstance(
                JavaServiceCallBufferHandlerTest.getClassLoader(),
                c,
                new ForwardingInvocationHandler( proxyService )
        )
    }

    @Unroll
    def "can handle buffer."()
    {
        expect:
        instance.canHandleBuffer( bufferType.getName() ) == result

        where:
        bufferType | result
        CasualBufferType.JSON_JSCD | true
        CasualBufferType.JSON | false
        CasualBufferType.FIELDED | false
    }

    def "fromRequest returns serviceinfo."()
    {
        given:
        InboundRequest request = InboundRequest.of( methodName, buffer )

        when:
        ServiceCallInfo info = instance.fromRequest( jndiObject, null, request )

        then:
        info.getMethod().get() == jndiObject.getClass().getMethod( methodName, String.class )
        info.getParams().length == 1
        info.getParams()[0] == methodParam
    }

    def "fromRequest multi payload throws exception"()
    {
        setup:
        payload.add( json.getBytes( StandardCharsets.UTF_8 ) )

        buffer = ServiceBuffer.of(CasualBufferType.JSON_JSCD.getName(), payload )

        InboundRequest request = InboundRequest.of( methodName, buffer )

        when:
        instance.fromRequest( jndiObject, null, request )

        then:
        thrown IllegalArgumentException
    }

    def "fromRequest method not found throws ServiceHandlerException"()
    {
        setup:
        serialisedCall = JavaServiceCallDefinition.of( "unknown", methodParam )
        json = jp.toJson( serialisedCall )
        payload.clear()
        payload.add( json.getBytes( StandardCharsets.UTF_8 ) )

        buffer = ServiceBuffer.of(CasualBufferType.JSON_JSCD.getName(), payload )

        InboundRequest request = InboundRequest.of( methodName, buffer )

        when:
        instance.fromRequest( jndiObject, null, request )

        then:
        thrown HandlerException
    }

    def "toResponse returns result response with buffer as CasualBuffer"()
    {
        setup:
        String result = "hello"
        String expected = '"hello"'

        when:
        InboundResponse actual = instance.toResponse( result )
        String payload = new String( actual.getBuffer().getBytes().get( 0 ), StandardCharsets.UTF_8 )

        then:
        actual.getBuffer().getType() == CasualBufferType.JSON_JSCD.getName()
        payload == expected
    }

    def "toResponse null result returns response with empty buffer"()
    {
        when:
        InboundResponse actual = instance.toResponse( null )

        then:
        actual.getBuffer().getType() == CasualBufferType.JSON_JSCD.getName()
        actual.getBuffer().getBytes().isEmpty()
    }

}
