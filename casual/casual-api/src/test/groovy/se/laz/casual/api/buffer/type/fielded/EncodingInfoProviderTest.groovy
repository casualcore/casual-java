/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded

import org.junit.Rule
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import org.junit.contrib.java.lang.system.EnvironmentVariables

// TODO: Remove this once https://github.com/stefanbirkner/system-rules is fixed for java 11
@IgnoreIf({ jvm.isJava11()  })
class EncodingInfoProviderTest extends Specification
{
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

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
        setup:
        environmentVariables.set(EncodingInfoProvider.FIELDED_ENCODING_PROPERTY_NAME, latinOneEncoding.name())
        when:
        def c = instance.getCharset()
        then:
        c == latinOneEncoding
    }

    def 'unknown charset name, should default to default encoding'()
    {
        setup:
        environmentVariables.set(EncodingInfoProvider.FIELDED_ENCODING_PROPERTY_NAME, 'this is not an encoding')
        when:
        def c = instance.getCharset()
        then:
        c == defaultEncoding
    }

}
