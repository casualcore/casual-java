/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedMarshaller;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessorMode;

import java.lang.reflect.Method;
import java.util.Objects;

public final class CasualFieldedMarshaller implements FieldedMarshaller
{
    @Override
    public FieldedTypeBuffer marshall(Object o)
    {
        return marshall(o, FieldedTypeBufferProcessorMode.RELAXED);
    }

    @Override
    public FieldedTypeBuffer marshall(Object o, FieldedTypeBufferProcessorMode mode)
    {
        Objects.requireNonNull(o, "object to be marshalled is not allowed to be null");
        Objects.requireNonNull("mode can not be null");
        FieldedTypeBuffer b = FieldedTypeBuffer.create();
        return Marshaller.write(o, b, mode);
    }

    @Override
    public <T> T unmarshall(FieldedTypeBuffer b, Class<T> clazz)
    {
        return unmarshall(b, clazz, FieldedTypeBufferProcessorMode.RELAXED);
    }

    @Override
    public <T> T unmarshall(FieldedTypeBuffer b, Class<T> clazz, FieldedTypeBufferProcessorMode mode)
    {
        Objects.requireNonNull(b, "fielded type buffer is not allowed to be null");
        Objects.requireNonNull(clazz, "clazz is not allowed to be null");
        Objects.requireNonNull(mode, "mode is not allowed to be null");
        // we make a copy of the buffer since operations that we will use are destructive
        // note: this is not very expensive since we use the same immutable references
        FieldedTypeBuffer copy = FieldedTypeBuffer.of(b);
        return Unmarshaller.createObject(copy, clazz, mode);
    }

    @Override
    public Object[] unmarshall(FieldedTypeBuffer b, Method m)
    {
        return unmarshall(b, m, FieldedTypeBufferProcessorMode.RELAXED);
    }

    @Override
    public Object[] unmarshall(FieldedTypeBuffer b, Method m, FieldedTypeBufferProcessorMode mode)
    {
        Objects.requireNonNull(b, "buffer can not be null");
        Objects.requireNonNull(m, "method can not be null");
        // we make a copy of the buffer since operations that we will use are destructive
        // note: this is not very expensive since we use the same immutable references
        FieldedTypeBuffer copy = FieldedTypeBuffer.of(b);
        return Unmarshaller.createMethodParameterObjects(copy, m, mode);
    }
}
