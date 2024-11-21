/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded

import se.laz.casual.config.ConfigurationOptions
import se.laz.casual.config.ConfigurationService
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets

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

    def cleanup()
    {
        ConfigurationService.reload(  )
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
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING, latinOneEncoding.name(  ) )

        def c

        when:
        c = instance.getCharset()

        then:
        c == latinOneEncoding
    }

    def 'unknown charset name, should default to default encoding'()
    {
        given:
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING, 'this is not an encoding' )
        def c

        when:
        c = instance.getCharset()

        then:
        c == defaultEncoding
    }

}
