package se.laz.casual.api.buffer.type


import se.laz.casual.api.buffer.CasualBufferType
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class CStringBufferTest extends Specification {

    @Shared
    def defaultCharset = Charset.defaultCharset()
    @Shared
    def nonDefaultCharset

    def setupSpec()
    {
        nonDefaultCharset = defaultCharset != StandardCharsets.UTF_8 ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1
    }

    def 'Wrong cstring creation from null value'()
    {
        when:
        CStringBuffer.of(null)
        then:
        def e = thrown(NullPointerException)
        !e.message.empty
    }

    def 'Wrong cstring creation with null charset'()
    {
        when:
        CStringBuffer.of("Foo", null)
        then:
        def e = thrown(NullPointerException)
        !e.message.empty
    }


    def 'already nullterminated Java string, does not modify'()
    {
        given:
        String data = "Hello Casual!" + "\0"
        byte[] bytes = data.getBytes(  )
        when:
        CStringBuffer buffer = CStringBuffer.of( [bytes] )

        then:
        buffer.getBytes(  )[0] == bytes
    }

    def 'two byte[]'()
    {
        when:
        CStringBuffer.of(["one".getBytes(), "two".getBytes()])
        then:
        def e = thrown(IllegalArgumentException)
        !e.message.empty
    }

    def 'CStringBuffer from String'()
    {
        setup:
        String s = "Hello Casual!"
        when:
        def v = CStringBuffer.of(s)
        then:
        noExceptionThrown()
        v.getType() == CasualBufferType.CSTRING.name
        v.toString() == s
        byte[] data = v.getBytes(  ).get(0)
        data[data.length-1] == "\0".getBytes(  )[0]
    }

    def 'CStringBuffer from null byte[]'()
    {
        setup:
        byte[] b = null
        when:
        CStringBuffer.of(b)
        then:
        def e = thrown(NullPointerException)
        !e.message.empty
    }

    def 'CStringBuffer from byte[] with null charset'()
    {
        setup:
        byte[] b = 'Foo'.getBytes()
        Charset charset = null
        when:
        CStringBuffer.of([b], charset)
        then:
        def e = thrown(NullPointerException)
        !e.message.empty
    }


    def 'CStringBuffer from byte[]'()
    {
        setup:
        String s = "Hello Casual!"
        String nullTerminated= s + "\0"
        byte[] b = nullTerminated.getBytes()
        when:
        def v = CStringBuffer.of([b])
        then:
        noExceptionThrown()
        v.getType() == CasualBufferType.CSTRING.name
        v.toString() == s
    }

    def 'CStringBuffer from byte[] without null termination, throws IllegalArgumentException.'()
    {
        setup:
        String s = "Hello Casual!"
        byte[] b = s.getBytes()
        when:
        CStringBuffer.of([b])
        then:
        thrown IllegalArgumentException
    }

    def 'CStringBuffer from byte[] with non default charset'()
    {
        setup:
        String s = "Hello Casual!"
        String nullTerminated = s + '\0'
        byte[] b = nullTerminated.getBytes(nonDefaultCharset)
        when:
        def buffer = CStringBuffer.of([b], nonDefaultCharset)
        then:
        buffer.toString() == s
        buffer.getCharset() == nonDefaultCharset
    }

    def 'platform encoding should be used when no encoding is specified'()
    {
        setup:
        String s = "Hello Casual!"
        String nullTerminated = s + '\0'
        byte[] b = nullTerminated.getBytes()
        when:
        def buffer = CStringBuffer.of([b])
        then:
        buffer.toString() == s
        buffer.getCharset() == defaultCharset
    }

    def 'verify that the provided charset is actually used'()
    {
        setup:
        String s = "Hello Casual! €"
        when:
        def buffer = CStringBuffer.of(s, StandardCharsets.UTF_8)
        then:
        s == buffer.toString()
    }
}
