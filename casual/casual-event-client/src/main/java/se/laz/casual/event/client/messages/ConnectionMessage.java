/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.client.messages;

// java:S6548 - yes it is intentional
@SuppressWarnings("java:S6548")
public class ConnectionMessage
{
    private static final String CONNECTION_MESSAGE = "{\"message\":\"HELLO\"}";
    private static final ConnectionMessage INSTANCE = new ConnectionMessage();
    public static Object of()
    {
        return INSTANCE;
    }
    public String getConnectionMessage()
    {
        return CONNECTION_MESSAGE;
    }
    @Override
    public String toString()
    {
        return CONNECTION_MESSAGE;
    }
}
