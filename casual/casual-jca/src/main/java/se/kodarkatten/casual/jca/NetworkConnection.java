package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;

import java.util.concurrent.CompletableFuture;

/**
 * Created by aleph on 2017-06-14.
 */
public interface NetworkConnection
{
    <T extends CasualNetworkTransmittable, X extends CasualNetworkTransmittable> CompletableFuture<CasualNWMessage<T>> request(CasualNWMessage<X> message);
    void close();
}
