/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api;

import se.laz.casual.api.queue.MessageSelector;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.queue.QueueMessage;

import java.util.List;
import java.util.UUID;

/**
 * Interface to operate on a casual queue
 */
public interface CasualQueueApi
{
    /**
     * Enqueue msg
     * @param qinfo the queue info
     * @param msg the queue message
     * @return the id of the enqueued message
     */
    UUID enqueue(QueueInfo qinfo, QueueMessage msg);

    /**
     * Dequeue message(s)
     * @param qinfo the queue info
     * @param selector the queue message
     * @return a list of matching messages
     */
    List<QueueMessage> dequeue(QueueInfo qinfo, MessageSelector selector);

    /**
     * Check if queue exists
     * @param qinfo the queue info
     * @return true if queue exists
     */
    boolean queueExists(QueueInfo qinfo);
}
