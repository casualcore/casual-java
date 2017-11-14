package se.kodarkatten.casual.api;

import se.kodarkatten.casual.api.queue.MessageSelector;
import se.kodarkatten.casual.api.queue.QueueInfo;
import se.kodarkatten.casual.api.queue.QueueMessage;

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
}
