/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.impl;

import se.laz.casual.api.buffer.type.fielded.FieldType;
import se.laz.casual.api.buffer.type.fielded.FieldedData;

import java.util.Arrays;
import java.util.Objects;

/**
 * Implementation of {@link FieldedData}
 * @param <T> the type
 */
//Serializble - T will be serializable by convention.
@SuppressWarnings("squid:S1948")
public final class FieldedDataImpl<T> implements FieldedData<T>
{
    private static final long serialVersionUID = 1L;

    private final T v;
    private final FieldType type;
    private FieldedDataImpl(final T v, FieldType t)
    {
        this.v = v;
        this.type = t;
    }

    /**
     * Creates an instance
     * @param v the value
     * @param t the field type
     * @param <T> the type
     * @return a new instance
     */
    public static <T> FieldedDataImpl<T> of(final T v, FieldType t)
    {
        Objects.requireNonNull(v, "value is not allowed to be null");
        return new FieldedDataImpl<>(v, t);
    }
    @Override
    public T getData()
    {
        return v;
    }

    @Override
    public <X> X getData(Class<X> clazz)
    {
        if(clazz.equals(Integer.class) && v.getClass().equals(Long.class))
        {
            return clazz.cast(Math.toIntExact((Long)v));
        }
        return clazz.cast(v);
    }

    @Override
    public FieldType getType()
    {
        return type;
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
        FieldedDataImpl<?> that = (FieldedDataImpl<?>) o;
        return v.getClass().isArray() ? Arrays.equals((byte[])v, (byte[])that.v) : Objects.equals(v, that.v);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(v);
    }
}
