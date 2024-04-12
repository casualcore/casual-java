package se.laz.casual.event.client.messages;

public class ConnectionMessage
{
    private static final String CONNECTION_MESSAGE = "{\"message\":\"HELLO\"}";
    public static Object of()
    {
        return new ConnectionMessage();
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
