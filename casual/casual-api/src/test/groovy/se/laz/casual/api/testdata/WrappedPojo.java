/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.testdata;

import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.io.Serializable;
import java.util.Objects;

public class WrappedPojo implements Serializable
{
    private static final long serialVersionUID = 1;
    // note: name is not needed for a wrapped fielded POJO
    // however, if it is provided it is just ignored
    @CasualFieldElement
    private final SimplePojo sp;
    @CasualFieldElement(name = "FLD_STRING1")
    private final String symbol;

    // non fielded field
    private final String sneakyField = "sneaky";
    private WrappedPojo(final SimplePojo sp, String symbol)
    {
        this.sp = sp;
        this.symbol = symbol;
    }
    private WrappedPojo()
    {
        sp = null;
        symbol = null;
    }
    public static WrappedPojo of(final SimplePojo sp, String symbol)
    {
        Objects.requireNonNull(sp);
        Objects.requireNonNull(symbol);
        return new WrappedPojo(sp, symbol);
    }

    public SimplePojo getSp()
    {
        return sp;
    }

    public String getSymbol()
    {
        return symbol;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        WrappedPojo that = (WrappedPojo) o;
        return Objects.equals(sp, that.sp) &&
            Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sp, symbol);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("WrappedPojo{");
        sb.append("sp=").append(sp);
        sb.append(", symbol='").append(symbol).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
