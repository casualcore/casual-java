/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type


import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class JsonBufferTest extends Specification
{
    @Shared JsonBuffer instance1, instance2
    @Shared byte[] data1 = "{This is the data1 message.}".getBytes(StandardCharsets.UTF_8)
    @Shared byte[] data1_copy = "{This is the data1 message.}".getBytes(StandardCharsets.UTF_8)
    @Shared byte[] data2 = "{This is the data2 message.}".getBytes(StandardCharsets.UTF_8)
    @Shared byte[] data2_copy = "{This is the data2 message.}".getBytes(StandardCharsets.UTF_8)

    @Unroll
    def "Equals and Hashcode permutations."()
    {
        given:
        instance1 = JsonBuffer.of( Arrays.asList( d1, d2 ) )
        instance2 = JsonBuffer.of( Arrays.asList( dc1, dc2 ) )

        expect:
        instance1.equals( instance2 ) == expectedResult
        ( instance1.hashCode() == instance2.hashCode() ) == expectedResult

        where:
        d1      | d2        | dc1       | dc2       | expectedResult
        data1   | data2     | data1_copy| data2_copy| true
        data1   | data2     | data1     | data2     | true
        data1   | data2     | data2     | data1     | false
    }

}
