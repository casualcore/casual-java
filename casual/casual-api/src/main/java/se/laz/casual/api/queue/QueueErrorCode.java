/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.queue;

import java.util.Arrays;

public enum QueueErrorCode
{
    OK(0),
    NO_MESSAGE(1),
    NO_QUEUE(10),
    ARGUMENT(20),
    SYSTEM(30),
    SIGNALED(40);
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
