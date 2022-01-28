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
import java.util.Objects;

public final class PojoWithMappableField implements Serializable
{
    private static final long serialVersionUID = 1;
    @CasualFieldElement(name ="FLD_STRING1", mapper = LocalDateMapper.class)
    private final LocalDate from;
    @CasualFieldElement(name ="FLD_STRING2", mapper = LocalDateMapper.class)
    private final LocalDate to;
    private PojoWithMappableField(final LocalDate from, final LocalDate to)
    {
        this.from = from;
        this.to = to;
    }
    // NOP-constructor needed
    private PojoWithMappableField()
    {
        from = null;
        to = null;
    }

    public static PojoWithMappableField of(final LocalDate from, final LocalDate to)
    {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        if(to.isBefore(from))
        {
            throw new IllegalArgumentException("to can not be before from");
        }
        return new PojoWithMappableField(from, to);
    }

    public LocalDate getFrom()
    {
        return from;
    }

    public LocalDate getTo()
    {
        return to;
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
        PojoWithMappableField that = (PojoWithMappableField) o;
        return Objects.equals(from, that.from) &&
            Objects.equals(to, that.to);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(from, to);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("PojoWithMappableField{");
        sb.append("from=").append(from);
        sb.append(", to=").append(to);
        sb.append('}');
        return sb.toString();
    }
}
