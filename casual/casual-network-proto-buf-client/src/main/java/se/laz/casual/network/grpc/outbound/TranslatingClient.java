package se.laz.casual.network.grpc.outbound;

import com.spotify.futures.ListenableFuturesExtra;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.network.messages.CasualGrpc;
import se.laz.casual.network.messages.CasualReply;
import se.laz.casual.network.messages.CasualRequest;

import java.util.concurrent.CompletableFuture;

/**
 * Test client that is easy and non intrusive to hook into current outbound implementation
 * so that we can start running functional and non functional tests and compare with our old implementation.
 */
public final class TranslatingClient implements NetworkConnection
{

    private final ManagedChannel channel;
    private final CasualGrpc.CasualFutureStub futureStub;

    private TranslatingClient(ManagedChannel channel)
    {
        this.channel = channel;
        this.futureStub = CasualGrpc.newFutureStub(channel);
    }

    public static NetworkConnection of(String host, int port)
    {
        return new TranslatingClient(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CasualNetworkTransmittable, X extends CasualNetworkTransmittable> CompletableFuture<CasualNWMessage<T>> request(CasualNWMessage<X> message)
    {
        CasualRequest request = RequestConverter.toCasualRequest(message);
        CompletableFuture<CasualReply> completableFuture = ListenableFuturesExtra.toCompletableFuture(futureStub.makeRequest(request));
        CompletableFuture<CasualNWMessage<T>> reply = new CompletableFuture<>();
        completableFuture.whenComplete((value, err) ->{
            if(err != null){
                reply.completeExceptionally(err);
                return;
            }
            reply.complete((CasualNWMessage<T>) ReplyConverter.toCasualNWMessage(value));
        });
        return reply;
    }

    @Override
    public void close()
    {
        channel.shutdownNow();
    }

    @Override
    public boolean isActive()
    {
        return channel.isShutdown();
    }
}
