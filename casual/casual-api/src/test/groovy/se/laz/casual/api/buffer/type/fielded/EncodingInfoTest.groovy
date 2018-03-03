/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded

import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class EncodingInfoTest extends Specification
{
    @Shared
    def defaultEncoding = StandardCharsets.UTF_8

    def 'should default to default encoding'()
    {
        when:
        def c = EncodingInfo.getCharset()
        then:
        c == defaultEncoding
    }
}
