package se.kodarkatten.casual.jca.inbound.handler

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class InboundRequestTest extends Specification
{

    @Shared InboundRequest instance
    @Shared String servicename = "servicename"
    @Shared List<byte[]> payload = Arrays.asList( "payload".getBytes( StandardCharsets.UTF_8 ) )
    @Shared List<byte[]> payload3 = Arrays.asList( "payload".getBytes( StandardCharsets.UTF_8 ) )

    @Shared String servicename2 = "servicename2"
    @Shared List<byte[]> payload2 = Arrays.asList( "payload2".getBytes( StandardCharsets.UTF_8 ) )

    def setup()
    {
        instance = InboundRequest.of( servicename, payload )
    }

    def cleanup()
    {
        instance = null
    }

    def "Get service name returns provided service name"()
    {
        expect:
        instance.getServiceName() == servicename
    }

    def "Get payload returns provided payload"()
    {
        expect:
        instance.getPayload() == payload
    }

    @Unroll
    def "of with nulls throws NullPointerException"()
    {
        when:
        InboundRequest.of( service, pay )

        then:
        thrown NullPointerException.class

        where:
        service | pay
        null    | payload
        servicename | null
        null    | null
    }

    @Unroll
    def "equals and hashcode test"()
    {
        when:
        InboundRequest instance2 = InboundRequest.of( service, pay )

        then:
        instance.equals( instance2 ) == result
        (instance.hashCode() == instance2.hashCode() ) == result

        where:
        service      | pay     || result
        servicename  | payload || true
        servicename  | payload3 || true
        servicename2 | payload || false
        servicename  | payload2 || false
        servicename2 | payload2 || false
    }

    def "equals self is true"()
    {
        expect:
        instance.equals( instance )
    }

    def "equals null is false"()
    {
        expect:
        ! instance.equals( null )
    }

    def "equals other object is false"()
    {
        expect:
        ! instance.equals( "123" )
    }

    def "toString returns value"()
    {
        expect:
        instance.toString() != null
    }

}
