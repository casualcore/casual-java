/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import io.netty.channel.ChannelId;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class CasualInboundTransactionRegistry
{
    private static final Logger log = Logger.getLogger(CasualInboundTransactionRegistry.class.getName());
    private final Map<ChannelId, Set<XidKey>> transactions = new ConcurrentHashMap<>();

    public void add(ChannelId channelId, XidKey key)
    {
        Objects.requireNonNull(key, "key can not be null");
        log.finest(() -> "Adding transaction " + key + " to inbound transaction registry");
        transactions.computeIfAbsent(channelId, id -> ConcurrentHashMap.newKeySet()).add(key);
    }
    public void remove(ChannelId channelId, XidKey key)
    {
        Objects.requireNonNull(key, "key can not be null");
        Objects.requireNonNull(channelId, "channelId can not be null");
        log.finest(() -> "Removing transaction " + key + " from transaction registry");
        Set<XidKey> ids = transactions.get(channelId);
        ids.remove(key);
        if(ids.isEmpty())
        {
            transactions.remove(channelId);
        }
    }

    public void remove(ChannelId channelId)
    {
        Objects.requireNonNull(channelId, "channelId can not be null");
        log.finest(() -> "Removing all pending transactions for " + channelId + " from transaction registry");
        transactions.remove(channelId);
    }

    public boolean hasPending()
    {
        log.finest(() -> "# of inbound pending: " + transactions.size());
        return !transactions.isEmpty();
    }
}
