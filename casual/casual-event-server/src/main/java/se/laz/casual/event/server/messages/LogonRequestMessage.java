package se.laz.casual.event.server.messages;

import java.util.Objects;

public class LogonRequestMessage
{
    private final LogonRequest message;
    private LogonRequestMessage(LogonRequest message)
    {
        this.message = message;
    }
    public static LogonRequestMessage of(LogonRequest message)
    {
        Objects.requireNonNull(message, "message can not be null");
        return new LogonRequestMessage(message);
    }

    @Override
    public String toString()
    {
        return "LogonRequestMessage{" +
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
        LogonRequestMessage that = (LogonRequestMessage) o;
        return message == that.message;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(message);
    }
}
