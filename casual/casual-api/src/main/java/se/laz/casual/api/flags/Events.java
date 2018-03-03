/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.flags;

/**
 * @author jone
 */
public enum Events
{
    TPEV_DISCONIMM(0x0001),
    TPEV_SVCERR(0x0002),
    TPEV_SVCFAIL(0x0004),
    TPEV_SVCSUCC(0x0008),
    TPEV_SENDONLY(0x0020);

    private final int value;

    Events(final int value)
    {
        this.value = value;
    }

    public final int getValue()
    {
        return value;
    }
}