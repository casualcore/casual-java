/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type

import se.laz.casual.api.buffer.CasualHeaders
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class JsonBufferTest extends Specification
{
    @Shared JsonBuffer instance1, instance2
    @Shared byte[] data1 = "{'data':'This is the data1 message.'}".getBytes(StandardCharsets.UTF_8)
    @Shared byte[] data1_copy = "{'data':'This is the data1 message.'}".getBytes(StandardCharsets.UTF_8)
    @Shared byte[] data2 = "{'data':'This is the data2 message.'}".getBytes(StandardCharsets.UTF_8)
    @Shared byte[] data2_copy = "{'data':'This is the data2 message.'}".getBytes(StandardCharsets.UTF_8)

    @Shared List<String> rawHeaders = ["a:foo","b:bar", "c:baz" ]
    @Shared CasualHeaders headers

    def setupSpec()
    {
        headers = CasualHeaders.newBuilder().addAll( rawHeaders ).build(  )
    }

    @Unroll
    def "Equals and Hashcode permutations."()
    {
        given:
        instance1 = JsonBuffer.of( Arrays.asList( d1, d2 ) )
        instance2 = JsonBuffer.of( Arrays.asList( dc1, dc2 ) )
        JsonBuffer instance3 = JsonBuffer.of( Arrays.asList( d1 ) )

        expect:
        instance1.equals( instance1 )
        instance1.equals( instance2 ) == expectedResult
        !instance1.equals( instance3)
        instance1.hashCode(  ) == instance1.hashCode()
        ( instance1.hashCode() == instance2.hashCode() ) == expectedResult

        where:
        d1      | d2        | dc1       | dc2       | expectedResult
        data1   | data2     | data1_copy| data2_copy| true
        data1   | data2     | data1     | data2     | true
        data1   | data2     | data2     | data1     | false
    }

    def "Check we don't break toString as it is used externally."()
    {
        given:
        String payload = "{'data':'this is the payload data that can't break.}"

        when:
        JsonBuffer instance = JsonBuffer.of( payload )

        then:
        instance.toString(  ) == payload
    }

    def "Create with null headers, throws NullPointerException."()
    {
        when:
        JsonBuffer.of( "{'data':'me'}", null )

        then:
        thrown NullPointerException

        when:
        JsonBuffer.of( [data1], null )

        then:
        thrown NullPointerException
    }

    def "Get default headers - empty."()
    {
        when:
        instance1 = JsonBuffer.of( new String( data1, StandardCharsets.UTF_8 ) )

        then:
        instance1.getHeaders(  ) == CasualHeaders.empty(  )
    }


    def "Get headers returns expected."()
    {
        given:
        instance1 = JsonBuffer.of( new String( data1, StandardCharsets.UTF_8 ), headers )

        when:
        CasualHeaders actual = instance1.getHeaders()

        then:
        actual == headers
    }

}
