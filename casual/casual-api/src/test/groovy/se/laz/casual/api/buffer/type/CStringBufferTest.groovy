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

}
