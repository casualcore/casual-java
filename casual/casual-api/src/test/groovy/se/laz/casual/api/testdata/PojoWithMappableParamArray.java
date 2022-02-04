/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.testdata;

import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.laz.casual.api.buffer.type.fielded.mapper.LocalDateMapper;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

public final class PojoWithMappableParamArray implements Serializable
{
    private static final long serialVersionUID = 1;
    private LocalDate[] dates;
    private PojoWithMappableParamArray(final LocalDate[] dates)
    {
        this.dates = dates;
    }
    // NOP-constructor needed
    private PojoWithMappableParamArray()
    {}
    public static PojoWithMappableParamArray of(final LocalDate[] dates)
    {
        Objects.requireNonNull(dates);
        return new PojoWithMappableParamArray(dates);
    }

    @CasualFieldElement(name ="FLD_STRING1", mapper = LocalDateMapper.class)
    public LocalDate[] getDates()
    {
        return dates;
    }

    public PojoWithMappableParamArray setDates(@CasualFieldElement(name ="FLD_STRING1", mapper = LocalDateMapper.class) LocalDate[] dates)
    {
        this.dates = dates;
        return this;
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
        PojoWithMappableParamArray that = (PojoWithMappableParamArray) o;
        return Arrays.equals(dates, that.dates);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(dates);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("PojoWithMappableFieldArray{");
        sb.append("dates=").append(Arrays.toString(dates));
        sb.append('}');
        return sb.toString();
    }
}
