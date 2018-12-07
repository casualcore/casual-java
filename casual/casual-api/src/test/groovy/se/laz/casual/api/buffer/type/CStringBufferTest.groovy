package se.laz.casual.api.buffer.type

import se.laz.casual.api.buffer.CasualBufferType
import spock.lang.Specification

class CStringBufferTest extends Specification
{
    def 'Wrong cstring creation from null value'()
    {
        when:
        CStringBuffer.of(null)
        then:
        def e = thrown(NullPointerException)
        !e.message.empty
    }


    def 'already nullterminated Java string'()
    {
        when:
        CStringBuffer.of("Hello Casual!" + "\0")
        then:
        def e = thrown(IllegalArgumentException)
        !e.message.empty
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

}
