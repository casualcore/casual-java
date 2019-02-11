/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.service

import se.laz.casual.api.buffer.type.ServiceBuffer
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

    @Shared byte[] bytes1 = "payload".getBytes( StandardCharsets.UTF_8 )
    @Shared byte[] bytes2 = "payload2".getBytes( StandardCharsets.UTF_8 )
    @Shared byte[] bytes3 = "payload".getBytes( StandardCharsets.UTF_8 )

    @Shared List<byte[]> payload =  new ArrayList<>()
    @Shared List<byte[]> payload2 =  new ArrayList<>()
    @Shared List<byte[]> payload3 =  new ArrayList<>()

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


}
