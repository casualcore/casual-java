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

public interface CasualQueueApi
{
    /**
     * Enqueue msg
     * @param qinfo
     * @param msg
     * @return
     */
    UUID enqueue(QueueInfo qinfo, QueueMessage msg);

    /**
     * Dequeue message(s)
     * @param qinfo
     * @param selector
     * @return
     */
    List<QueueMessage> dequeue(QueueInfo qinfo, MessageSelector selector);

    /**
     * Check if queue exists
     * @param qinfo
     * @return true if queue exists
     */
    boolean queueExists(QueueInfo qinfo);
}
