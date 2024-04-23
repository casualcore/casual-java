/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.server.messages;

import se.laz.casual.api.CasualTypeException;

import java.util.Arrays;

public enum ConnectReply
{
    CONNECT_REPLY("WELCOME");
    private final String value;
    ConnectReply(String value)
    {
        this.value = value;
    }
    String getValue()
    {
        return value;
    }
    static ConnectReply unmarshall(String in)
    {
        return Arrays.stream(values())
                     .filter(v -> v.value.equals(in))
                     .findFirst()
                     .orElseThrow(() -> new CasualTypeException("Unknown type: " + in));
    }
}

