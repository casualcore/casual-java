/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api;

import se.laz.casual.api.queue.DequeueReturn;
import se.laz.casual.api.queue.EnqueueReturn;
import se.laz.casual.api.queue.MessageSelector;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.queue.QueueMessage;

/**
 * Interface to operate on a casual queue
 */
public interface CasualQueueApi
{
    /**
     * Enqueue msg
     * @param qinfo the queue info
     * @param msg the queue message
     * @throws se.laz.casual.network.connection.CasualConnectionException
     * @return a wrapper containing the uuid of the enqueued message and an ErrorState
     */
    EnqueueReturn enqueue(QueueInfo qinfo, QueueMessage msg);

    /**
     * Dequeue message(s)
     * @param qinfo the queue info
     * @param selector the queue message
     * @throws se.laz.casual.network.connection.CasualConnectionException
     * @return a wrapper containing a list of matching messages and an ErrorState
     */
    DequeueReturn dequeue(QueueInfo qinfo, MessageSelector selector);

    /**
     * Check if queue exists
     * @param qinfo the queue info
     * @throws se.laz.casual.network.connection.CasualConnectionException
     * @return true if queue exists
     */
    boolean queueExists(QueueInfo qinfo);
}
