/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded


import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

class EncodingInfoProviderTest extends Specification
{

    @Shared
    def defaultEncoding = StandardCharsets.UTF_8
    @Shared
    def latinOneEncoding = StandardCharsets.ISO_8859_1
    @Shared
    EncodingInfoProvider instance

    def setup()
    {
        instance = EncodingInfoProvider.of()
    }

    def 'no env set, default encoding'()
    {
        when:
        def c = instance.getCharset()
        then:
        c == defaultEncoding
    }

    def 'env set to latin 1'()
    {
        given:
        def c

        when:
        withEnvironmentVariable(EncodingInfoProvider.FIELDED_ENCODING_ENV_NAME, latinOneEncoding.name()).execute( {
            c = instance.getCharset()
        } )

        then:
        c == latinOneEncoding
    }

    def 'unknown charset name, should default to default encoding'()
    {
        given:
        def c

        when:
        withEnvironmentVariable(EncodingInfoProvider.FIELDED_ENCODING_ENV_NAME, 'this is not an encoding').execute( {
            c = instance.getCharset()
        } )

        then:
        c == defaultEncoding
    }

}
