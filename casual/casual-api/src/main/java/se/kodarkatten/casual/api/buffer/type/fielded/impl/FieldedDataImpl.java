package se.kodarkatten.casual.api.buffer.type.fielded.impl;

import se.kodarkatten.casual.api.buffer.type.fielded.FieldType;
import se.kodarkatten.casual.api.buffer.type.fielded.FieldedData;

import java.util.Arrays;
import java.util.Objects;

public final class FieldedDataImpl<T> implements FieldedData<T>
{
    private final T v;
    private final FieldType type;
    private FieldedDataImpl(final T v, FieldType t)
    {
        this.v = v;
        this.type = t;
    }
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
