/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class CorrelatorImpl implements Correlator
{
    private static final Logger log = Logger.getLogger(CorrelatorImpl.class.getName());
    private final Map<UUID, CompletableFuture<?>> requests = new ConcurrentHashMap<>();
    private CorrelatorImpl()
    {}
    public static CorrelatorImpl of()
    {
        return new CorrelatorImpl();
    }

    @Override
    public void put(final UUID corrid, final CompletableFuture<?> f)
    {
        requests.put(corrid, f);
    }
    @Override
    public <T extends CasualNetworkTransmittable>  void complete(final CasualNWMessage<T> msg)
    {
        @SuppressWarnings("unchecked")
        CompletableFuture<CasualNWMessage<T>> f = (CompletableFuture<CasualNWMessage<T>>)requests.remove(msg.getCorrelationId());
        if(null == f)
        {
            // log failure, this is an inconsistency that should not occur
            log.warning(() -> "Can not find a future for correlation id: " + msg.getCorrelationId() + " this should NEVER happen!");
            return;
        }
        if(!f.isCancelled())
        {
            f.complete(msg);
        }
    }

    @Override
    public boolean isEmpty()
    {
        return requests.isEmpty();
    }

    @Override
    public void completeExceptionally(final List<UUID> l, final Exception e)
    {
        l.stream()
         .forEach(v -> completeExceptionally(requests.remove(v), e));
    }

    @Override
    public void completeAllExceptionally(Exception e)
    {
        completeExceptionally(requests.keySet().stream()
                                      .collect(Collectors.toList()), e);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        CorrelatorImpl that = (CorrelatorImpl) o;
        return Objects.equals(requests, that.requests);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(requests);
    }

    private void completeExceptionally(final CompletableFuture<?> f, final Exception e)
    {
        Objects.requireNonNull(f, "future can not be null");
        if(!f.isCancelled())
        {
            f.completeExceptionally(e);
        }
    }
}
