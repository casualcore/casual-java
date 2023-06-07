/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;

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
