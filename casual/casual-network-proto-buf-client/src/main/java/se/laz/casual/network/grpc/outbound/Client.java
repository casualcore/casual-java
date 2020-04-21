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

public final class Client implements NetworkConnection
{

    private final ManagedChannel channel;
    private final CasualGrpc.CasualFutureStub futureStub;

    private Client(ManagedChannel channel)
    {
        this.channel = channel;
        this.futureStub = CasualGrpc.newFutureStub(channel);
    }

    public static NetworkConnection of(String host, int port)
    {
        return new Client(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
    }

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
            reply.complete(ReplyConverter.toCasualNWMessage(value));
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
