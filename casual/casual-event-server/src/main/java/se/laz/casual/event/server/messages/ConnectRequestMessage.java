/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server.messages;

import java.util.Objects;

public class ConnectRequestMessage
{
    private final ConnectRequest message;
    private ConnectRequestMessage(ConnectRequest message)
    {
        this.message = message;
    }
    public static ConnectRequestMessage of(ConnectRequest message)
    {
        Objects.requireNonNull(message, "message can not be null");
        return new ConnectRequestMessage(message);
    }

    @Override
    public String toString()
    {
        return "ConnectRequestMessage{" +
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
        ConnectRequestMessage that = (ConnectRequestMessage) o;
        return message == that.message;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(message);
    }
}
