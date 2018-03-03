/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer.fielded

import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.CasualBufferType
import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer
import se.laz.casual.jca.inbound.handler.buffer.ServiceCallInfo
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Method
import java.lang.reflect.Proxy

class FieldedBufferHandlerTest extends Specification
{
    @Shared FieldedBufferHandler instance

    @Shared Proxy jndiObject
    @Shared String methodName = "echo"
    @Shared Method method
    @Shared String methodParam = "method param1"
    @Shared SimpleObject methodObject = SimpleObject.of( methodParam )
    @Shared SimpleService proxyService

    @Shared FieldedTypeBuffer buffer

    def setup()
    {
        instance = new FieldedBufferHandler()

        method = SimpleService.getMethod( methodName, SimpleObject.class )
        buffer =FieldedTypeBuffer.create().write('FLD_STRING1', methodParam )

        Class[] c = new Class[1]
        c[0] = SimpleService.class
        proxyService = Mock( SimpleService )

        jndiObject = (Proxy)Proxy.newProxyInstance(
                FieldedBufferHandlerTest.getClassLoader(),
                c,
                new ForwardingInvocationHandler( proxyService )
        )
    }

    @Unroll
    def "canHandleBuffer"()
    {
        expect:
        instance.canHandleBuffer( bufferType.getName() ) == result

        where:
        bufferType                 | result
        CasualBufferType.FIELDED   | true
        CasualBufferType.JSON      | false
        CasualBufferType.JSON_JSCD | false
    }

    def "fromBuffer returns service call info"()
    {
        when:
        ServiceCallInfo info = instance.fromBuffer( jndiObject, method, buffer )

        then:
        info.getMethod().get() == method
        info.getParams().length == 1
        info.getParams()[0].getClass() == SimpleObject.class
        ((SimpleObject)info.getParams()[0]) == methodObject
    }

    def "toBuffer result returns in buffer"()
    {
        setup:
        SimpleObject result = SimpleObject.of( "hello" )

        when:
        CasualBuffer buffer = instance.toBuffer( result )

        then:
        buffer.getType() == CasualBufferType.FIELDED.getName()

        ((FieldedTypeBuffer)buffer).peek( "FLD_STRING1" ).get().getData(String.class) == "hello"
    }

    def "toBuffer result is null returns empty buffer"()
    {
        when:
        CasualBuffer buffer = instance.toBuffer( null )

        then:
        buffer.getType() == CasualBufferType.FIELDED.getName()
        buffer.getBytes().isEmpty()
    }
}
