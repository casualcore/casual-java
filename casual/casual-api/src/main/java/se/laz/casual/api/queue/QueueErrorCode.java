package se.laz.casual.api.queue;

import java.util.Arrays;

public enum QueueErrorCode
{
    ok(0),
    no_message(1),
    no_queue(10),
    argument(20),
    system(30),
    signaled(40);
    private final int value;

    QueueErrorCode(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public static final QueueErrorCode unmarshal(int id)
    {
        return Arrays.stream(QueueErrorCode.values())
                     .filter(entry -> entry.value == id)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown QueueErrorCode:" + id));
    }

    public static final int marshal(QueueErrorCode s)
    {
        return s.value;
    }
}
