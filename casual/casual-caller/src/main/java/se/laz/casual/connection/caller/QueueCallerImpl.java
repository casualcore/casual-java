package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.DequeueReturn;
import se.laz.casual.api.queue.EnqueueReturn;
import se.laz.casual.api.queue.MessageSelector;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.queue.QueueMessage;

public class QueueCallerImpl implements QueueCaller
{
    @Override
    public EnqueueReturn enqueue(QueueInfo qinfo, QueueMessage msg)
    {
        return null;
    }

    @Override
    public DequeueReturn dequeue(QueueInfo qinfo, MessageSelector selector)
    {
        return null;
    }
}
