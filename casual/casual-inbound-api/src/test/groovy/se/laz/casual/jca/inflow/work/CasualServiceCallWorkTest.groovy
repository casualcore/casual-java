/*
 * Copyright (c) 2017 - 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow.work

import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.type.JavaServiceCallDefinition
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.external.json.JsonProvider
import se.laz.casual.api.external.json.JsonProviderFactory
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.api.xa.XID
import se.laz.casual.jca.inbound.handler.InboundRequest
import se.laz.casual.jca.inbound.handler.InboundResponse
import se.laz.casual.jca.inbound.handler.service.ServiceHandler
import se.laz.casual.jca.inflow.handler.test.TestHandler
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import se.laz.casual.api.buffer.type.ServiceBuffer
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

class CasualServiceCallWorkTest extends Specification
{
    @Shared CasualServiceCallWork instance
    @Shared CasualServiceCallWork instanceTPNOREPLY
    @Shared UUID correlationId
    @Shared CasualServiceCallRequestMessage message

    @Shared JavaServiceCallDefinition serialisedCall
    @Shared String jndiServiceName = "se.laz.casual.test.Service"
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

        CompletableFuture<Long> timeFuture = new CompletableFuture<>()
        instance = new CasualServiceCallWork(correlationId, message, timeFuture)
        instance.setHandler( handler )

        instanceTPNOREPLY = new CasualServiceCallWork( correlationId, message, true, timeFuture )
        instanceTPNOREPLY.setHandler( handler )
        timeFuture.complete(42L)
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
        InboundResponse response = InboundResponse.createBuilder().buffer( buffer).build()
        1 * handler.invokeService( _ as InboundRequest ) >> { InboundRequest request ->
            actualRequest = request
            return response
        }

        when:
        instance.run()
        CasualNWMessage<CasualServiceCallReplyMessage> reply = instance.getResponse()

        then:
        reply.getMessage().getError() == ErrorState.OK
        reply.getMessage().getTransactionState() == TransactionState.TX_ACTIVE;
        reply.getMessage().getUserDefinedCode() == 0L
        String j = new String( reply.getMessage().getServiceBuffer().getPayload().get( 0 ), StandardCharsets.UTF_8 )
        jp.fromJson( j, String.class ) == methodParam

        actualRequest.getServiceName() == jndiServiceName
        actualRequest.getBuffer().getBytes() == JsonBuffer.of( json ).getBytes()
    }

    def "Call Service with buffer and return InboundResponse tx, error and user defined error codes in result."()
    {
        given:
        InboundRequest actualRequest = null
        ErrorState expectedErrorState = ErrorState.TPESVCERR;
        TransactionState expectedTransactionState = TransactionState.TIMEOUT_ROLLBACK_ONLY;
        long expectedUserDefinedCode = 12L
        InboundResponse response = InboundResponse.createBuilder()
                .buffer( buffer)
                .errorState( expectedErrorState )
                .transactionState( expectedTransactionState )
                .userSuppliedErrorCode( expectedUserDefinedCode )
                .build()
        1 * handler.invokeService( _ as InboundRequest ) >> { InboundRequest request ->
            actualRequest = request
            return response
        }

        when:
        instance.run()
        CasualNWMessage<CasualServiceCallReplyMessage> reply = instance.getResponse()

        then:
        reply.getMessage().getError() == expectedErrorState;
        reply.getMessage().getTransactionState() == expectedTransactionState
        reply.getMessage().getUserDefinedCode() == expectedUserDefinedCode
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
        InboundResponse response = InboundResponse.createBuilder()
                .buffer( JsonBuffer.of( jp.toJson( exceptionMessage ) ) )
                .errorState( ErrorState.TPESVCERR )
                .transactionState( TransactionState.TIMEOUT_ROLLBACK_ONLY )
                .build()
        1 * handler.invokeService( _ as InboundRequest ) >> { InboundRequest request ->
            actualRequest = request
            return response
        }

        when:
        instance.run()
        CasualNWMessage<CasualServiceCallReplyMessage> reply = instance.getResponse()

        then:
        reply.getMessage().getError() == ErrorState.TPESVCERR
        reply.getMessage().getTransactionState() == TransactionState.TIMEOUT_ROLLBACK_ONLY
        String j = new String( reply.getMessage().getServiceBuffer().getPayload().get( 0 ), StandardCharsets.UTF_8 )
        j.contains( exceptionMessage )

        actualRequest.getServiceName() == jndiServiceName
        actualRequest.getBuffer().getBytes() == JsonBuffer.of( json ).getBytes()
    }

    def "Call Service, TPNOREPLY, with buffer - there should be no reply"()
    {
       given:
       InboundRequest actualRequest = null
       InboundResponse response = InboundResponse.createBuilder().buffer( buffer).build()
       1 * handler.invokeService( _ as InboundRequest ) >> { InboundRequest request ->
          actualRequest = request
          return response
       }

       when:
       instanceTPNOREPLY.run()
       CasualNWMessage<CasualServiceCallReplyMessage> reply = instanceTPNOREPLY.getResponse()

       then:
       reply == null
       actualRequest.getServiceName() == jndiServiceName
       actualRequest.getBuffer().getBytes() == JsonBuffer.of( json ).getBytes()
    }

    def "Release does nothing."()
    {
        given:
        InboundRequest actualRequest = null
        InboundResponse response = InboundResponse.createBuilder().buffer( buffer).build()
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
        CompletableFuture<Long> timeFuture = new CompletableFuture<>()
        instance = new CasualServiceCallWork(correlationId, message, timeFuture)
        when:
        instance.run()
        timeFuture.complete(42L)
        CasualNWMessage<CasualServiceCallReplyMessage> reply = instance.getResponse()

        then:
        reply.getMessage().getError() == ErrorState.TPENOENT
    }
}
