/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import se.laz.casual.api.CasualRuntimeException;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.protocol.messages.conversation.Request;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class ConversationMessageStorageImpl implements ConversationMessageStorage
{
    private static final Map<UUID, BlockingDeque<CasualNWMessage<Request>>> storage = new ConcurrentHashMap<>();

    private ConversationMessageStorageImpl()
    {}

    public static ConversationMessageStorage of()
    {
        return new ConversationMessageStorageImpl();
    }

    @Override
    public Optional<CasualNWMessage<Request>> nextMessage(UUID corrId)
    {
        BlockingDeque<CasualNWMessage<Request>> queue = getQueue(corrId);
        return queue.isEmpty() ? Optional.empty() : Optional.of(queue.remove());
    }

    @Override
    public CasualNWMessage<Request> takeFirst(UUID corrId)
    {
        try
        {
            BlockingDeque<CasualNWMessage<Request>> queue = getQueue(corrId);
            return queue.takeFirst();
        }
        catch(InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new CasualRuntimeException("ConversationMessageStorage::takeFirst interrupted");
        }
    }

    @Override
    public void put(UUID corrId, CasualNWMessage<Request> message)
    {
        Objects.requireNonNull(corrId, "corrId can not be null");
        Objects.requireNonNull(message, "message can not be null");
        BlockingDeque<CasualNWMessage<Request>> queue = getQueue(corrId);
        queue.add(message);
    }

    @Override
    public int size(UUID corrId)
    {
        if(null == storage.get(corrId))
        {
            return 0;
        }
        BlockingDeque<CasualNWMessage<Request>> queue = getQueue(corrId);
        return queue.size();
    }

    @Override
    public void clear(UUID corrId)
    {
        storage.remove(corrId);
    }

    @Override
    public int numberOfConversations()
    {
        return storage.size();
    }

    @Override
    public void clearAllConversations()
    {
        storage.clear();
    }

    public static void remove(UUID corrId)
    {
        storage.remove(corrId);
    }

    private BlockingDeque<CasualNWMessage<Request>> getQueue(UUID corrId)
    {
        storage.putIfAbsent(corrId, new LinkedBlockingDeque<>());
        return storage.get(corrId);
    }

}
