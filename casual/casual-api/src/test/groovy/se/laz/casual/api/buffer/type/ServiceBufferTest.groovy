/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type

import se.laz.casual.api.buffer.CasualHeaders
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class ServiceBufferTest extends Specification
{
    @Shared ServiceBuffer instance
    @Shared String type = "testtype"
    @Shared String type2 = "testtype2"
    @Shared String type3 = "testtype"
    @Shared String noType = ''

    @Shared byte[] bytes1 = "payload".getBytes( StandardCharsets.UTF_8 )
    @Shared byte[] bytes2 = "payload2".getBytes( StandardCharsets.UTF_8 )
    @Shared byte[] bytes3 = "payload".getBytes( StandardCharsets.UTF_8 )

    @Shared List<byte[]> payload =  new ArrayList<>()
    @Shared List<byte[]> payload2 =  new ArrayList<>()
    @Shared List<byte[]> payload3 =  new ArrayList<>()
    @Shared List<byte[]> noPayload = new ArrayList<>()

    @Shared List<String> rawHeaders = ["a:foo","b:bar", "c:baz" ]
    @Shared CasualHeaders headers

    def setupSpec()
    {
        headers = CasualHeaders.newBuilder().addAll( rawHeaders ).build(  )
    }

    def setup()
    {
        payload.add( bytes1 )
        payload2.add( bytes2 )
        payload3.add( bytes3 )

        instance = ServiceBuffer.of( type, payload )
    }

    def "Get type"()
    {
        expect:
        instance.getType() == type
    }

    def "Get payload"()
    {
        expect:
        instance.getPayload() == payload
    }

    @Unroll
    def "equals and hashcode only allow referential equality."()
    {
        setup:
        ServiceBuffer instance2 = ServiceBuffer.of( t, p )

        expect:
        instance.equals( instance2 ) == result
        (instance.hashCode() == instance2.hashCode()) == result

        where:
        t     | p        | result
        type  | payload  | false
        type2 | payload  | false
        type  | payload2 | false
        type3 | payload3 | false
        type2 | payload3 | false
        type3 | payload2 | false
    }

    def 'no type and no payload'()
    {
        when:
        def s = ServiceBuffer.empty()
        then:
        s.isEmpty()
    }

    def 'type and no payload'()
    {
        when:
        ServiceBuffer.of(type, noPayload)
        then:
        noExceptionThrown()
    }

    def 'payload and no type'()
    {
        when:
        ServiceBuffer.of(noType, payload)
        then:
        thrown(CasualProtocolException)
    }

    def 'empty equality'()
    {
        when:
        def b = ServiceBuffer.of(noType, noPayload)
        then:
        b.isEmpty()
        ServiceBuffer.empty() == b
    }

    def 'empty self equality'()
    {
        when:
        def b = ServiceBuffer.empty()
        then:
        b.isEmpty()
        ServiceBuffer.empty() == b
    }

    def "Get headers default - empty."()
    {
        when:
        CasualHeaders actual = buffer.getHeaders()

        then:
        actual == CasualHeaders.empty()

        where:
        buffer << [
                ServiceBuffer.empty(  ),
                ServiceBuffer.nullBuffer(  ),
                ServiceBuffer.of( type, payload ),
                ServiceBuffer.of( CStringBuffer.of( "hi" ) )
        ]
    }

    def "Get headers."()
    {
        when:
        CasualHeaders actual = buffer.getHeaders()

        then:
        actual == expected

        where:
        buffer                                                | expected
        ServiceBuffer.empty()                                 | CasualHeaders.empty()
        ServiceBuffer.nullBuffer()                            | CasualHeaders.empty()
        ServiceBuffer.of( type, payload, headers )            | headers
        ServiceBuffer.of( CStringBuffer.of( "hi", headers ) ) | headers
    }

    def "Create with null headers, throws NullPointerException."()
    {
        when:
        ServiceBuffer.of( type, payload, null )

        then:
        thrown NullPointerException
    }

}
