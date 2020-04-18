package se.laz.casual.network.grpc.inbound


import com.spotify.futures.ListenableFuturesExtra
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import org.junit.Rule
import se.laz.casual.api.CasualRuntimeException
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualGrpc
import se.laz.casual.network.messages.CasualReply
import se.laz.casual.network.messages.CasualRequest
import se.laz.casual.network.messages.CasualServiceCallReply
import se.laz.casual.network.messages.CasualServiceCallRequest
import se.laz.casual.network.messages.TransactionState
import se.laz.casual.network.messages.XID
import spock.lang.Specification

import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

class CasualServerTest extends Specification
{
    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule()

    /**
     * To test the server, make calls with a real stub using the in-process channel, and verify
     * behaviors or state changes from the client side.
     */
    def 'echo test service call'()
    {
        given:
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName()
        def msg = 'Hello World!'
        CasualRequest request = createServiceCallRequest(msg.getBytes())
        final RequestDelegate delegate = Mock()
        with(delegate){
            1 * handleRequest(request) >> {
                request.getMessageType() == CasualRequest.MessageType.SERVICE_CALL_REQUEST
                return createServiceCallReply(request)
            }
        }
        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(CasualServer.CasualServerImpl.of(delegate)).build().start())

        CasualGrpc.CasualBlockingStub blockingStub = CasualGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()))

        when:
        CasualReply reply = blockingStub.makeRequest(request)
        then:
        reply.hasServiceCall() == true
        when:
        CasualServiceCallReply serviceCallReply = reply.getServiceCall()
        then:
        new String(serviceCallReply.getPayload().toByteArray()) == msg.reverse()
    }

    def 'service call, exception in request delegate'()
    {
        given:
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName()
        def msg = 'Hello World!'
        CasualRequest request = createServiceCallRequest(msg.getBytes())
        final RequestDelegate delegate = Mock()
        with(delegate){
            1 * handleRequest(request) >> {
                // notice, request handlers should not do this in general
                // especially not for a service call - just package the exception in the payload of the response
                request.getMessageType() == CasualRequest.MessageType.SERVICE_CALL_REQUEST
                throw new IndexOutOfBoundsException()
            }
        }
        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(CasualServer.CasualServerImpl.of(delegate)).build().start())

        CasualGrpc.CasualFutureStub futureStub = CasualGrpc.newFutureStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()))

        when:
        CompletableFuture<CasualReply> completableFuture = ListenableFuturesExtra.toCompletableFuture(futureStub.makeRequest(request))
        try
        {
            completableFuture.join()
        }
        catch(CompletionException | CancellationException exception)
        {
            throw new CasualRuntimeException(exception)
        }
        then:
        def e = thrown(CasualRuntimeException)
        completableFuture.completedExceptionally == true
        when:
        StringWriter w = new StringWriter()
        e.printStackTrace(new PrintWriter(w))
        then:
        w.toString().contains('IndexOutOfBoundsException') == true
    }


    CasualRequest createServiceCallRequest(byte[] payload)
    {
        XID xid = MessageCreator.createXID(2, 2, 42, 'asdf'.getBytes())
        CasualServiceCallRequest serviceCallRequest = MessageCreator.createCasualServiceCallRequest(UUID.randomUUID(),'echo',0,Optional.empty(), xid, 0, 'TestType', payload)
        return MessageCreator.createRequestBuilder(CasualRequest.MessageType.SERVICE_CALL_REQUEST, UUID.randomUUID())
                             .setServiceCall(serviceCallRequest)
                             .build()
    }

    CasualReply createServiceCallReply(CasualRequest request)
    {
        CasualServiceCallRequest serviceCallRequest = request.getServiceCall()
        CasualServiceCallReply serviceCallReply = MessageCreator.createCasualServiceCallReply(MessageCreator.toUUID(serviceCallRequest.getExecution()), 0, 0,
                                                                                              serviceCallRequest.getXid(), TransactionState.TX_ACTIVE,
                                                                                              serviceCallRequest.getBufferTypeName(),
                                                                                              new String(serviceCallRequest.getPayload().toByteArray()).reverse().getBytes())
        return MessageCreator.createReplyBuilder(CasualReply.MessageType.SERVICE_CALL_REPLY, MessageCreator.toUUID(request.getCorrelationId()))
                             .setServiceCall(serviceCallReply)
                             .build()
    }
}
