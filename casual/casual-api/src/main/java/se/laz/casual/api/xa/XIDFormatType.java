/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.xa;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by aleph on 2017-03-15.
 */

/**
 * These are the only known types but any type is fine
 * Just means we can not display them in a good way
 */
public enum XIDFormatType
{
    NULL(-1l);
    private final long type;
    XIDFormatType(long type)
    {
        this.type = type;
    }

    public long getType()
    {
        return type;
    }

    public static long marshal(XIDFormatType t)
    {
        return t.getType();
    }

    public static final Optional<XIDFormatType> unmarshal(long n)
    {
        return  Arrays.stream(XIDFormatType.values())
                      .filter(v -> v.getType() == n)
                      .findFirst();
    }

    public static boolean isNullType(long n)
    {
        Optional<XIDFormatType> t = unmarshal(n);
        return t.isPresent() && t.get() == XIDFormatType.NULL;
    }

}
