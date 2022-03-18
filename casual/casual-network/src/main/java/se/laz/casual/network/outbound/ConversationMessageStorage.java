/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.protocol.messages.conversation.Request;

import java.util.Optional;
import java.util.UUID;

public interface ConversationMessageStorage
{
    Optional<CasualNWMessage<Request>> nextMessage(UUID corrid);
    void put(UUID corrid, CasualNWMessage<Request> message);

    /**
     * Blocks until a message is available
     * @return the first message available
     */
    CasualNWMessage<Request> takeFirst(UUID corrid);
    int size(UUID corrid);
    void clear(UUID corrid);
    int numberOfConversations();
    void clearAllConversations();
}
