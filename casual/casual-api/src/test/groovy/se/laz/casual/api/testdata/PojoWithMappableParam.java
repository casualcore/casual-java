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

public final class PojoWithMappableParam implements Serializable
{
    private static final long serialVersionUID = 1;
    private LocalDate from;
    private LocalDate to;
    private PojoWithMappableParam(final LocalDate from, final LocalDate to)
    {
        this.from = from;
        this.to = to;
    }
    // NOP-constructor needed
    private PojoWithMappableParam()
    {}
    public static PojoWithMappableParam of(final LocalDate from, final LocalDate to)
    {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        if(to.isBefore(from))
        {
            throw new IllegalArgumentException("to can not be before from");
        }
        return new PojoWithMappableParam(from, to);
    }

    @CasualFieldElement(name ="FLD_STRING1", mapper = LocalDateMapper.class)
    public LocalDate getFrom()
    {
        return from;
    }
    public PojoWithMappableParam setFrom(@CasualFieldElement(name ="FLD_STRING1", mapper = LocalDateMapper.class) LocalDate from)
    {
        this.from = from;
        return this;
    }

    @CasualFieldElement(name ="FLD_STRING2", mapper = LocalDateMapper.class)
    public LocalDate getTo()
    {
        return to;
    }
    public PojoWithMappableParam setTo(@CasualFieldElement(name ="FLD_STRING2", mapper = LocalDateMapper.class) LocalDate to)
    {
        this.to = to;
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
        PojoWithMappableParam that = (PojoWithMappableParam) o;
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
