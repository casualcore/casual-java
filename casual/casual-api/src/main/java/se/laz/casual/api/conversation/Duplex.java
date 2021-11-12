/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.conversation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Duplex
{
    SEND((short)0),
    RECEIVE((short)1);

    private static final Map<Integer, Duplex> values = new HashMap<>();
    private final short value;

    static
    {
        values.put(0, SEND);
        values.put(1, RECEIVE);
    }

    Duplex(short value)
    {
        this.value = value;
    }

    public short getValue()
    {
        return value;
    }

    public static Duplex unmarshall(int code)
    {
        return Optional.ofNullable(values.get(code)).orElseThrow(() ->new IllegalArgumentException("No duplex for code: " + code));
    }

}
