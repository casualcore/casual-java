/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling;

import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.buffer.type.fielded.marshalling.details.Marshaller;
import se.laz.casual.api.buffer.type.fielded.marshalling.details.Unmarshaller;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * marshall/unmarshall FieldedTypeBuffer
 * methods or fields annotated with @CasualFieldElement
 * will be taken into consideration
 *
 */
public final class FieldedTypeBufferProcessor
{
    private FieldedTypeBufferProcessor()
    {}

    public static FieldedTypeBuffer marshall(final Object o)
    {
        return marshall(o, FieldedTypeBufferProcessorMode.RELAXED);
    }

    public static FieldedTypeBuffer marshall(final Object o, FieldedTypeBufferProcessorMode mode)
    {
        Objects.requireNonNull(o, "object to be marshalled is not allowed to be null");
        FieldedTypeBuffer b = FieldedTypeBuffer.create();
        return Marshaller.write(o, b, mode);
    }

    public static <T> T unmarshall(final FieldedTypeBuffer b, final Class<T> clazz)
    {
        return unmarshall(b, clazz, FieldedTypeBufferProcessorMode.RELAXED);
    }

    public static <T> T unmarshall(final FieldedTypeBuffer b, final Class<T> clazz, FieldedTypeBufferProcessorMode mode)
    {
        Objects.requireNonNull(b, "fielded type buffer is not allowed to be null");
        Objects.requireNonNull(clazz, "clazz is not allowed to be null");
        // we make a copy of the buffer since operations that we will use are destructive
        // note: this is not very expensive since we use the same immutable references
        FieldedTypeBuffer copy = FieldedTypeBuffer.of(b);
        return Unmarshaller.createObject(copy, clazz, mode);
    }

    public static Object[] unmarshall(final FieldedTypeBuffer b, final Method m)
    {
        return unmarshall(b, m, FieldedTypeBufferProcessorMode.RELAXED);
    }

    public static Object[] unmarshall(final FieldedTypeBuffer b, final Method m, FieldedTypeBufferProcessorMode mode)
    {
        Objects.requireNonNull(b, "buffer can not be null");
        Objects.requireNonNull(m, "method can not be null");
        // we make a copy of the buffer since operations that we will use are destructive
        // note: this is not very expensive since we use the same immutable references
        FieldedTypeBuffer copy = FieldedTypeBuffer.of(b);
        return Unmarshaller.createMethodParameterObjects(copy, m, mode);
    }

}
