/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class LocalDateMapper implements CasualObjectMapper<LocalDate, String>
{
    @Override
    public String to(LocalDate src)
    {
        return src.format(DateTimeFormatter.ISO_DATE);
    }

    @Override
    public LocalDate from(String dst)
    {
        return LocalDate.parse(dst, DateTimeFormatter.ISO_DATE);
    }

    @Override
    public Class<?> getDstType()
    {
        return String.class;
    }
}
