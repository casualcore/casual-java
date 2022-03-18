/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer;

import java.util.Arrays;
import java.util.Optional;

/**
 * The known buffer types for which there is support in casual-api
 * Note that it is fine with any buffer type but if it is not supported
 * then user code has to handle it
 */
public enum CasualBufferType
{
    JSON(".json/"),
    JSON_JSCD(".json/jscd"),
    FIELDED("CFIELD/"),
    CSTRING("CSTRING/"),
    X_OCTET("X_OCTET/");
    private final String name;
    CasualBufferType(final String name)
    {
        this.name = name;
    }
    public static CasualBufferType unmarshall(final String name)
    {
        Optional<CasualBufferType> t = Arrays.stream(CasualBufferType.values())
                                             .filter(v -> v.getName().equals(name))
                                             .findFirst();
        return t.orElseThrow(() -> new IllegalArgumentException("CasualBufferType:" + name));
    }

    public static String marshall(CasualBufferType type)
    {
        return type.getName();
    }

    public String getName()
    {
        return name;
    }

}
