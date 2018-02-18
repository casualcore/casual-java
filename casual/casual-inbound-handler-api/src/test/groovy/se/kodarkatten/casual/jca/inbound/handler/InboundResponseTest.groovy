package se.kodarkatten.casual.jca.inbound.handler

import se.kodarkatten.casual.api.buffer.CasualBuffer
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class InboundResponseTest extends Specification
{

    @Shared InboundResponse instance
    @Shared List<byte[]> payload = Arrays.asList( "buffer".getBytes( StandardCharsets.UTF_8 ) )
    @Shared boolean success = true
    @Shared CasualBuffer buffer = ServiceBuffer.of( "test", payload )

    @Shared List<byte[]> payload2 = Arrays.asList( "payload2".getBytes( StandardCharsets.UTF_8 ) )
    @Shared List<byte[]> payload3 = Arrays.asList( "buffer".getBytes( StandardCharsets.UTF_8 ) )

    @Shared CasualBuffer buffer2 = ServiceBuffer.of( "test", payload2 )
    @Shared CasualBuffer buffer3 = ServiceBuffer.of( "test", payload3 )

    def setup()
    {
        instance = InboundResponse.of( success, buffer )
    }

    def cleanup()
    {
        instance = null
    }

    def "is successful returns provided value."()
    {
        expect:
        instance.isSuccessful()
    }

    def "get payload returns provided value."()
    {
        expect:
        instance.getBuffer() == buffer
    }

    @Unroll
    def "equals and hashcode only allows referential equality."()
    {
        when:
        InboundResponse instance2 = InboundResponse.of( successful, buf )

        then:
        instance.equals( instance2 ) == result
        (instance.hashCode() == instance2.hashCode() ) == result

        where:
        successful | buf   || result
        true       | buffer  || false
        true       | buffer3 || false
        false      | buffer  || false
        true       | buffer2 || false
        false      | buffer2 || false
    }

    def "of null check throws NullPointer"()
    {
        when:
        InboundResponse.of( success, null )

        then:
        thrown NullPointerException.class
    }

    def "equals self is true"()
    {
        expect:
        instance.equals( instance )
    }

    def "equals other type is false"()
    {
        expect:
        ! instance.equals( "123" )
    }

    def "equals null is false"()
    {
        expect:
        ! instance.equals( null )
    }

    def "toString returns value."()
    {
        expect:
        instance.toString() != null
    }
}

