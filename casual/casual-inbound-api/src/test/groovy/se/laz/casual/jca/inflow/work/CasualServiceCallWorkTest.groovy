/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow.work

import com.google.protobuf.ByteString
import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.type.JavaServiceCallDefinition
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.external.json.JsonProvider
import se.laz.casual.api.external.json.JsonProviderFactory
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.xa.XID
import se.laz.casual.jca.inbound.handler.InboundRequest
import se.laz.casual.jca.inbound.handler.InboundResponse
import se.laz.casual.jca.inbound.handler.service.ServiceHandler
import se.laz.casual.jca.inflow.handler.test.TestHandler
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualReply
import se.laz.casual.network.messages.CasualServiceCallReply
import se.laz.casual.network.messages.CasualServiceCallRequest
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class CasualServiceCallWorkTest extends Specification
{
    @Shared CasualServiceCallWork instance
    @Shared UUID correlationId
    @Shared CasualServiceCallRequest message

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

        message = CasualServiceCallRequest.newBuilder()
                .setXid(MessageCreator.toXID(XID.NULL_XID))
                .setExecution(MessageCreator.toUUID4(UUID.randomUUID()))
                .setServiceName(jndiServiceName)
                .setBufferTypeName('json')
                .setPayload(ByteString.copyFrom(json.getBytes()))
                .setFlags(0)
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
        instance.getRequest() == message
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
        CasualReply replyEnvelope = instance.getResponse()
        CasualServiceCallReply reply = replyEnvelope.getServiceCall()

        then:
        ErrorState.valueOf(reply.getResult().name()) == ErrorState.OK
        MessageCreator.toTransactionState(reply.getTransactionState()) == TransactionState.TX_ACTIVE;
        reply.getUser() == 0L
        String j = new String( reply.getPayload().toByteArray(), StandardCharsets.UTF_8 )
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
        CasualReply replyEnvelope = instance.getResponse()
        CasualServiceCallReply reply = replyEnvelope.getServiceCall()
        then:
        ErrorState.valueOf(reply.getResult().name()) == expectedErrorState
        MessageCreator.toTransactionState(reply.getTransactionState()) == expectedTransactionState
        reply.getUser() == expectedUserDefinedCode
        String j = new String( reply.getPayload().toByteArray(), StandardCharsets.UTF_8 )
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
        CasualReply replyEnvelope = instance.getResponse()
        CasualServiceCallReply reply = replyEnvelope.getServiceCall()

        then:
        ErrorState.valueOf(reply.getResult().name()) == ErrorState.TPESVCERR
        MessageCreator.toTransactionState(reply.getTransactionState()) == TransactionState.TIMEOUT_ROLLBACK_ONLY
        String j = new String( reply.getPayload().toByteArray(), StandardCharsets.UTF_8 )
        j.contains( exceptionMessage )

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

        CasualReply replyEnvelope = instance.getResponse()
        CasualServiceCallReply reply = replyEnvelope.getServiceCall()

        then:
        ErrorState.valueOf(reply.getResult().name()) == ErrorState.OK
        String json = new String( reply.getPayload().toByteArray(), StandardCharsets.UTF_8 )
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
        CasualReply replyEnvelope = instance.getResponse()
        CasualServiceCallReply reply = replyEnvelope.getServiceCall()

        then:
        ErrorState.valueOf(reply.getResult().name()) == ErrorState.TPENOENT
    }
}
