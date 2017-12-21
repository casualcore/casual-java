package se.kodarkatten.casual.jca.inbound.handler

import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class InboundResponseTest extends Specification
{

    @Shared InboundResponse instance
    @Shared List<byte[]> payload = Arrays.asList( "payload".getBytes( StandardCharsets.UTF_8 ) )
    @Shared boolean success = true

    @Shared List<byte[]> payload2 = Arrays.asList( "payload2".getBytes( StandardCharsets.UTF_8 ) )
    @Shared List<byte[]> payload3 = Arrays.asList( "payload".getBytes( StandardCharsets.UTF_8 ) )

    def setup()
    {
        instance = InboundResponse.of( success, payload )
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
        instance.getPayload() == payload
    }

    def "equals and hashcode checks."()
    {
        when:
        InboundResponse instance2 = InboundResponse.of( successful, pay )

        then:
        instance.equals( instance2 ) == result
        (instance.hashCode() == instance2.hashCode() ) == result

        where:
        successful | pay      || result
        true       | payload  || true
        true       | payload3 || true
        false      | payload  || false
        true       | payload2 || false
        false      | payload2 || false
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

