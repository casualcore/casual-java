package se.laz.casual.network.outbound;

import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNetworkTransmittable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Correlator
{
    boolean isEmpty();
    void completeExceptionally(final List<UUID> l, final Exception e);
    void completeAllExceptionally(final Exception e);
    void put(final UUID corrid, final CompletableFuture<?> f);
    <T extends CasualNetworkTransmittable>  void complete(final CasualNWMessage<T> msg);
}
