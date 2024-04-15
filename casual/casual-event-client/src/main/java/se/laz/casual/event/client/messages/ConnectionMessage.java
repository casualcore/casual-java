package se.laz.casual.event.client.messages;

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
