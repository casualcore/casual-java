package se.kodarkatten.casual.internal.network;

import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNetworkTransmittable;

import java.util.concurrent.CompletableFuture;

/**
 * Created by aleph on 2017-06-14.
 */
public interface NetworkConnection
{
    <T extends CasualNetworkTransmittable, X extends CasualNetworkTransmittable> CompletableFuture<CasualNWMessage<T>> request(CasualNWMessage<X> message);
    void close();
}
