/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.queue;

import se.laz.casual.api.queue.QueueMessage;
import se.laz.casual.network.protocol.messages.queue.DequeueMessage;

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
