/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.flags;

/**
 * The possible casual error states
 * @author jone
 */
public enum ErrorState
{
    OK(0),
    TPEBADDESC(2),
    TPEBLOCK(3),
    TPEINVAL(4),
    TPELIMIT(5),
    TPENOENT(6),
    TPEOS(7),
    TPEPROTO(9),
    TPESVCERR(10),
    TPESVCFAIL(11),
    TPESYSTEM(12),
    TPETIME(13),
    TPETRAN(14),
    TPGOTSIG(15),
    TPEITYPE(17),
    TPEOTYPE(18),
    TPEEVENT(22),
    TPEMATCH(23);

    private final int value;

    ErrorState(final int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public static final ErrorState unmarshal(int id)
    {
        for (ErrorState type : ErrorState.values())
        {
            if (type.getValue() == id)
            {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ErrorState type:" + id);
    }

    public static final int marshal(ErrorState s)
    {
        return s.getValue();
    }

}