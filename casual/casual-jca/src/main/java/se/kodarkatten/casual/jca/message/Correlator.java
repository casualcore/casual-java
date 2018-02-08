package se.kodarkatten.casual.jca.message;

import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Correlator
{
    boolean isEmpty();
    void completeExceptionally(final List<UUID> l, final Exception e);
    List<UUID> getAllCorrids();
    void forget(final UUID correlationId);
    void put(final UUID corrid, final CompletableFuture<?> f);
    <T extends CasualNetworkTransmittable>  void complete(final CasualNWMessage<T> msg);
}
