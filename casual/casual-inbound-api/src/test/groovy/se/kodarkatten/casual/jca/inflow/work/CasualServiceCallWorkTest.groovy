package se.kodarkatten.casual.jca.inflow.work

import se.kodarkatten.casual.api.buffer.CasualBuffer
import se.kodarkatten.casual.api.buffer.type.JavaServiceCallDefinition
import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.external.json.JsonProvider
import se.kodarkatten.casual.api.external.json.JsonProviderFactory
import se.kodarkatten.casual.api.flags.ErrorState
import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.jca.inbound.handler.InboundRequest
import se.kodarkatten.casual.jca.inbound.handler.InboundResponse
import se.kodarkatten.casual.jca.inbound.handler.service.ServiceHandler
import se.kodarkatten.casual.jca.inflow.handler.test.TestHandler
import se.kodarkatten.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import se.kodarkatten.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class CasualServiceCallWorkTest extends Specification
{
    @Shared CasualServiceCallWork instance
    @Shared UUID correlationId
    @Shared CasualServiceCallRequestMessage message

    @Shared JavaServiceCallDefinition serialisedCall
    @Shared String jndiServiceName = "se.kodarkatten.casual.test.Service"
    @Shared String methodName = "echo"
    @Shared String methodParam = "method param1"
    @Shared JsonProvider jp = JsonProviderFactory.getJsonProvider()
    @Shared String json

    @Shared ServiceHandler handler
    @Shared List<byte[]> payload
    @Shared CasualBuffer buffer


    def setup()
    {
        handler = Mock( ServiceHandler )
        payload = new ArrayList<>()
        byte[] p = jp.toJson( methodParam ).getBytes( StandardCharsets.UTF_8 )
        payload.add( p )
        buffer = JsonBuffer.of( payload )

        serialisedCall = JavaServiceCallDefinition.of( methodName, methodParam )

        json = jp.toJson( serialisedCall )

        message = CasualServiceCallRequestMessage.createBuilder()
                        .setXid( XID.NULL_XID)
                        .setExecution(UUID.randomUUID())
                        .setServiceName( jndiServiceName )
                        .setServiceBuffer( ServiceBuffer.of( "json",
                                                JsonBuffer.of(
                                                        json )
                                                        .getBytes() ) )
                        .setXatmiFlags( Flag.of())
                        .build()

        correlationId = UUID.randomUUID()

        instance = new CasualServiceCallWork( correlationId, message )
        instance.setHandler( handler )
    }

    def "Get header."()
    {
        expect:
        instance.getCorrelationId() == correlationId
    }

    def "Get message."()
    {
        expect:
        instance.getMessage() == message
    }

    def "Get Response."()
    {
        expect:
        instance.getResponse() == null
    }

    def "Call Service with buffer and return result."()
    {
        given:
        InboundRequest actualRequest = null
        InboundResponse response = InboundResponse.of( true, buffer)
        1 * handler.invokeService( _ as InboundRequest ) >> { InboundRequest request ->
            actualRequest = request
            return response
        }

        when:
        instance.run()
        CasualNWMessage<CasualServiceCallReplyMessage> reply = instance.getResponse()

        then:
        reply.getMessage().getError() == ErrorState.OK
        String j = new String( reply.getMessage().getServiceBuffer().getPayload().get( 0 ), StandardCharsets.UTF_8 )
        jp.fromJson( j, String.class ) == methodParam

        actualRequest.getServiceName() == jndiServiceName
        actualRequest.getBuffer().getBytes() == JsonBuffer.of( json ).getBytes()

    }

    def "Call Service with buffer service throws exception return ErrorState.TPSVCERR."()
    {
        given:
        InboundRequest actualRequest = null
        String exceptionMessage = "Simulated failure."
        InboundResponse response = InboundResponse.of( false, JsonBuffer.of( jp.toJson( exceptionMessage ) ) )
        1 * handler.invokeService( _ as InboundRequest ) >> { InboundRequest request ->
            actualRequest = request
            return response
        }

        when:
        instance.run()
        CasualNWMessage<CasualServiceCallReplyMessage> reply = instance.getResponse()

        then:
        reply.getMessage().getError() == ErrorState.TPESVCERR
        String j = new String( reply.getMessage().getServiceBuffer().getPayload().get( 0 ), StandardCharsets.UTF_8 )
        j.contains( exceptionMessage )

        actualRequest.getServiceName() == jndiServiceName
        actualRequest.getBuffer().getBytes() == JsonBuffer.of( json ).getBytes()
    }

    def "Release does nothing."()
    {
        given:
        InboundRequest actualRequest = null
        InboundResponse response = InboundResponse.of( true, buffer)
        1 * handler.invokeService( _ as InboundRequest ) >> { InboundRequest request ->
            actualRequest = request
            return response
        }

        when:
        instance.release()
        instance.run()
        CasualNWMessage<CasualServiceCallReplyMessage> reply = instance.getResponse()

        then:
        reply.getMessage().getError() == ErrorState.OK
        String json = new String( reply.getMessage().getServiceBuffer().getPayload().get( 0 ), StandardCharsets.UTF_8 )
        jp.fromJson( json, String.class ) == methodParam
    }
    def "getHandler returns when empty"()
    {
        given:
        instance.setHandler( null )

        when:
        ServiceHandler handler = instance.getHandler( TestHandler.SERVICE_1 )

        then:
        handler.getClass() == TestHandler.class
    }

    def "Call Service which does not exist or is not available, returns result with TPNOENT status."()
    {
        given:
        instance = new CasualServiceCallWork( correlationId, message )

        when:
        instance.run()
        CasualNWMessage<CasualServiceCallReplyMessage> reply = instance.getResponse()

        then:
        reply.getMessage().getError() == ErrorState.TPENOENT
    }
}
