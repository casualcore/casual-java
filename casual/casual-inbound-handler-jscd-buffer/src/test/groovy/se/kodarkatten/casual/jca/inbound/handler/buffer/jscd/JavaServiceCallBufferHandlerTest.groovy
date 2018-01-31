package se.kodarkatten.casual.jca.inbound.handler.buffer.jscd

import se.kodarkatten.casual.api.buffer.CasualBuffer
import se.kodarkatten.casual.api.buffer.CasualBufferType
import se.kodarkatten.casual.api.buffer.type.JavaServiceCallDefinition
import se.kodarkatten.casual.api.external.json.JsonProvider
import se.kodarkatten.casual.api.external.json.JsonProviderFactory
import se.kodarkatten.casual.jca.inbound.handler.buffer.ServiceCallInfo
import se.kodarkatten.casual.jca.inbound.handler.HandlerException
import se.kodarkatten.casual.network.messages.service.ServiceBuffer
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

    def "fromBuffer returns serviceinfo."()
    {
        when:
        ServiceCallInfo info = instance.fromBuffer( jndiObject, null, buffer )

        then:
        info.getMethod().get() == jndiObject.getClass().getMethod( methodName, String.class )
        info.getParams().length == 1
        info.getParams()[0] == methodParam
    }

    def "fromBuffer multi payload throws exception"()
    {
        setup:
        payload.add( json.getBytes( StandardCharsets.UTF_8 ) )

        buffer = ServiceBuffer.of(CasualBufferType.JSON_JSCD.getName(), payload )

        when:
        instance.fromBuffer( jndiObject, null, buffer )

        then:
        thrown IllegalArgumentException
    }

    def "fromBuffer method not found throws ServiceHandlerException"()
    {
        setup:
        serialisedCall = JavaServiceCallDefinition.of( "unknown", methodParam )
        json = jp.toJson( serialisedCall )
        payload.clear()
        payload.add( json.getBytes( StandardCharsets.UTF_8 ) )

        buffer = ServiceBuffer.of(CasualBufferType.JSON_JSCD.getName(), payload )

        when:
        instance.fromBuffer( jndiObject, null, buffer )

        then:
        thrown HandlerException
    }

    def "toBuffer returns result as CasualBuffer"()
    {
        setup:
        String result = "hello"
        String expected = '"hello"'

        when:
        CasualBuffer actual = instance.toBuffer( result )
        String payload = new String( actual.getBytes().get( 0 ), StandardCharsets.UTF_8 )

        then:
        actual.getType() == CasualBufferType.JSON_JSCD.getName()
        payload == expected
    }

    def "toBuffer null result returns empty buffer"()
    {
        when:
        CasualBuffer actual = instance.toBuffer( null )

        then:
        actual.getType() == CasualBufferType.JSON_JSCD.getName()
        actual.getBytes().isEmpty()
    }

}
