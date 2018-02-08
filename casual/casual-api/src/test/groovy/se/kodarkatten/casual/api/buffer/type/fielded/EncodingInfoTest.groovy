package se.kodarkatten.casual.api.buffer.type.fielded

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
