/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.flags;

/**
 * @author jone
 */
public enum BufferType
{
    X_OCTET("X_OCTET"),
    X_C_TYPE("X_C_TYPE"),
    X_COMMON("X_COMMON");

    private final String name;

    BufferType(String name)
    {
        this.name = name;
    }

    public final String getName()
    {
        return name;
    }
}