/*
 * Copyright (c) 2025 - 2026, The casual project. All rights reserved.
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
    private static final String CHANNEL_ID_CAN_NOT_BE_NULL = "channelId can not be null";
    private static final Map<ChannelId, Set<XidKey>> transactions = new ConcurrentHashMap<>();

    public void add(ChannelId channelId, XidKey key)
    {
        Objects.requireNonNull(channelId, CHANNEL_ID_CAN_NOT_BE_NULL);
        Objects.requireNonNull(key, "key can not be null");
        log.finest(() -> "adding transaction " + key + " for channel id: " + channelId + " to inbound transaction registry");
        transactions.computeIfAbsent(channelId, id -> ConcurrentHashMap.newKeySet()).add(key);
    }
    public void remove(ChannelId channelId, XidKey key)
    {
        Objects.requireNonNull(channelId, CHANNEL_ID_CAN_NOT_BE_NULL);
        Objects.requireNonNull(key, "key can not be null");
        log.finest(() -> "removing transaction " + key + "for channel id: " + channelId +" from transaction registry");
        transactions.computeIfPresent(channelId, (id, set) ->{
           set.remove(key);
           return set.isEmpty() ? null : set;
        });
    }

    public void remove(ChannelId channelId)
    {
        Objects.requireNonNull(channelId, CHANNEL_ID_CAN_NOT_BE_NULL);
        log.finest(() -> "Removing all pending transactions for " + channelId + " from transaction registry");
        transactions.remove(channelId);
    }

    public static boolean hasPending()
    {
        log.finest(() -> "# of inbound pending: " + transactions.size());
        return !transactions.isEmpty();
    }
}
