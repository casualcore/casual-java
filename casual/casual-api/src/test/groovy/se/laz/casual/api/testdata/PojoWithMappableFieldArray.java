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

public final class PojoWithMappableFieldArray implements Serializable
{
    private static final long serialVersionUID = 1;
    @CasualFieldElement(name ="FLD_STRING1", lengthName = "FLD_LONG1", mapper = LocalDateMapper.class)
    private final LocalDate[] dates;
    @CasualFieldElement(name ="FLD_STRING2", mapper = LocalDateMapper.class)
    private final LocalDate[] moreDates;

    private PojoWithMappableFieldArray(final LocalDate[] dates, final LocalDate[] moreDates)
    {
        this.dates = dates;
        this.moreDates = moreDates;
    }

    // NOP-constructor needed
    private PojoWithMappableFieldArray()
    {
        dates = null;
        moreDates = null;
    }

    public static PojoWithMappableFieldArray of(final LocalDate[] dates, final LocalDate[] moreDates)
    {
        Objects.requireNonNull(dates);
        Objects.requireNonNull(moreDates);
        return new PojoWithMappableFieldArray(dates, moreDates);
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
        PojoWithMappableFieldArray that = (PojoWithMappableFieldArray) o;
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
