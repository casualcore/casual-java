/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.flags;

import se.laz.casual.api.flags.internal.CasualFlag;

/**
 * Flag Bits
 * The following constants are the flag bits defined for the C-language XATMI routines:
 */
public enum AtmiFlags implements CasualFlag
{
    NOFLAG(0),
    TPNOBLOCK(0x00000001),
    TPSIGRSTRT(0x00000002),
    TPNOREPLY(0x00000004),
    TPNOTRAN(0x00000008),
    TPTRAN(0x00000010),
    TPNOTIME(0x00000020),
    TPGETANY(0x00000080),
    TPNOCHANGE(0x00000100),
    TPCONV(0x00000400),
    TPSENDONLY(0x00000800),
    TPRECVONLY(0x00001000);

    private final int value;

    AtmiFlags(int value)
    {
        this.value = value;
    }

    @Override
    public final int getValue() {
        return value;
    }

}