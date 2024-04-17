/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server.messages;

import java.util.Objects;

public class ConnectReplyMessage
{
    private final String message;
    private ConnectReplyMessage(ConnectReply message)
    {
        this.message = message.getValue();
    }
    public static ConnectReplyMessage of(ConnectReply message)
    {
        Objects.requireNonNull(message, "message can not be null");
        return new ConnectReplyMessage(message);
    }

    @Override
    public String toString()
    {
        return "ConnectReplyMessage{" +
                "message=" + message +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ConnectReplyMessage that = (ConnectReplyMessage) o;
        return message == that.message;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(message);
    }
}
