/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.utils

import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException
import se.laz.casual.config.ConfigurationOptions
import se.laz.casual.config.ConfigurationService
import spock.lang.Specification

class DecoderReaderValidatorTest extends Specification
{
    boolean originalStrictDataValidation

    def setup()
    {
        originalStrictDataValidation = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_NETWORK_PROTOCOL_STRICT_DATA_VALIDATION )
    }

    def cleanup()
    {
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_NETWORK_PROTOCOL_STRICT_DATA_VALIDATION, originalStrictDataValidation )
    }

    def "matching data length passes regardless of strict validation: #strictValidation"()
    {
        given:
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_NETWORK_PROTOCOL_STRICT_DATA_VALIDATION, strictValidation )

        when:
        DecoderReaderValidator.throwIfDataNotFullyRead( 10, 10 )

        then:
        noExceptionThrown()

        where:
        strictValidation << [true, false]
    }

    def "strict validation rejects unread data"()
    {
        given:
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_NETWORK_PROTOCOL_STRICT_DATA_VALIDATION, true )

        when:
        DecoderReaderValidator.throwIfDataNotFullyRead( 8, 10 )

        then:
        thrown(CasualProtocolException)
    }

    def "disabled strict validation permits unread data"()
    {
        given:
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_NETWORK_PROTOCOL_STRICT_DATA_VALIDATION, false )

        when:
        DecoderReaderValidator.throwIfDataNotFullyRead( 8, 10 )

        then:
        noExceptionThrown()
    }
}
