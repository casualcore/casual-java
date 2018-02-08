package se.kodarkatten.casual.api.buffer.type.fielded

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

    def 'no property set, default encoding'()
    {
        when:
        def c = instance.getCharset()
        then:
        c == defaultEncoding
    }

    def 'property set to latin 1'()
    {
        setup:
        System.setProperty(EncodingInfoProvider.FIELDED_ENCODING_PROPERTY_NAME, latinOneEncoding.name())
        when:
        def c = instance.getCharset()
        then:
        c == latinOneEncoding
    }

    def 'unknown charset name, should default to default encoding'()
    {
        setup:
        System.setProperty(EncodingInfoProvider.FIELDED_ENCODING_PROPERTY_NAME, 'this is not an encoding')
        when:
        def c = instance.getCharset()
        then:
        c == defaultEncoding
    }

}
