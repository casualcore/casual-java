package se.kodarkatten.casual.jca.queue;

import se.kodarkatten.casual.api.queue.QueueMessage;
import se.kodarkatten.casual.network.protocol.messages.queue.DequeueMessage;

import java.util.List;
import java.util.stream.Collectors;

public final class Transformer
{
    private Transformer()
    {}
    public static List<QueueMessage> transform(final List<DequeueMessage> l)
    {
        return l.stream()
                .map(Transformer::transformMessage)
                .collect(Collectors.toList());
    }

    private static QueueMessage transformMessage(final DequeueMessage msg)
    {
        return QueueMessage.createBuilder()
                           .withId(msg.getId())
                           .withAvailableSince(msg.getAvailableSince())
                           .withPayload(msg.getPayload())
                           .withCorrelationInformation(msg.getCorrelationInformation())
                           .withRedelivered(msg.getNumberOfRedelivered())
                           .withReplyQueue(msg.getReplyQueue())
                           .withTimestamp(msg.getTimestamp())
                           .build();
    }

}
