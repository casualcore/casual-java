/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler

import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.type.ServiceBuffer
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class InboundRequestTest extends Specification
{

    @Shared InboundRequest instance
    @Shared String servicename = "servicename"

    @Shared List<byte[]> payload = Arrays.asList( "buffer".getBytes( StandardCharsets.UTF_8 ) )
    @Shared List<byte[]> payload3 = Arrays.asList( "buffer".getBytes( StandardCharsets.UTF_8 ) )
    @Shared CasualBuffer buffer = ServiceBuffer.of( "test", payload )
    @Shared CasualBuffer buffer3 = ServiceBuffer.of( "test", payload3 )

    @Shared String servicename2 = "servicename2"
    @Shared List<byte[]> payload2 = Arrays.asList( "payload2".getBytes( StandardCharsets.UTF_8 ) )
    @Shared CasualBuffer buffer2 = ServiceBuffer.of( "test2", payload2 )

    def setup()
    {
        instance = InboundRequest.of( servicename, buffer )
    }

    def cleanup()
    {
        instance = null
    }

    def "Get service name returns provided service name"()
    {
        expect:
        instance.getServiceName() == servicename
    }

    def "Get payload returns provided payload"()
    {
        expect:
        instance.getBuffer() == buffer
    }

    @Unroll
    def "of with nulls throws NullPointerException"()
    {
        when:
        InboundRequest.of( service, thebuffer )

        then:
        thrown NullPointerException.class

        where:
        service | thebuffer
        null    | buffer
        servicename | null
        null    | null
    }

    @Unroll
    def "equals and hashcode only allows referential equality"()
    {
        when:
        InboundRequest instance2 = InboundRequest.of( service, buf )

        then:
        instance.equals( instance2 ) == result
        (instance.hashCode() == instance2.hashCode() ) == result

        where:
        service      | buf   || result
        servicename  | buffer  || false
        servicename  | buffer3 || false
        servicename2 | buffer  || false
        servicename  | buffer2 || false
        servicename2 | buffer2 || false
    }

    def "equals self is true"()
    {
        expect:
        instance.equals( instance )
    }

    def "equals null is false"()
    {
        expect:
        ! instance.equals( null )
    }

    def "equals other object is false"()
    {
        expect:
        ! instance.equals( "123" )
    }

    def "toString returns value"()
    {
        expect:
        instance.toString() != null
    }

}
