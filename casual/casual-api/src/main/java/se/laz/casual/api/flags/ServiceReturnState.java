/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.flags;

/**
 * The state of a service call
 * @author jone
 */
public enum ServiceReturnState
{
    TPFAIL(0x0001),
    TPSUCCESS(0x0002);

    private final int value;

    ServiceReturnState(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}