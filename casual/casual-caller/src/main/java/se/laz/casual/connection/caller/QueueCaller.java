/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.DequeueReturn;
import se.laz.casual.api.queue.EnqueueReturn;
import se.laz.casual.api.queue.MessageSelector;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.queue.QueueMessage;

public interface QueueCaller
{
    EnqueueReturn enqueue(QueueInfo qinfo, QueueMessage msg);
    DequeueReturn dequeue(QueueInfo qinfo, MessageSelector selector);
    boolean queueExists(QueueInfo qinfo);
}
