/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.mapper;

/**
 * The default object mapper
 */
public final class PassThroughMapper implements CasualObjectMapper<Object, Object>
{
    @Override
    public Object to(Object src)
    {
        return src;
    }

    @Override
    public Object from(Object dst)
    {
        return dst;
    }

    @Override
    public Class<?> getDstType()
    {
        return Object.class;
    }
}
