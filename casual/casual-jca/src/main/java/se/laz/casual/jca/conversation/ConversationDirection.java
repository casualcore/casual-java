/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.conversation;

public enum ConversationDirection
{
    SEND,
    RECEIVE;

    ConversationDirection switchDirection()
    {
        return this == ConversationDirection.SEND ? RECEIVE : SEND;
    }

    boolean isReceive()
    {
        return this == RECEIVE;
    }

    boolean isSend()
    {
        return this == SEND;
    }

}
