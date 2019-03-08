/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling;

import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.laz.casual.api.buffer.type.fielded.marshalling.details.Marshaller;
import se.laz.casual.api.buffer.type.fielded.marshalling.details.Unmarshaller;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * marshall/unmarshall FieldedTypeBuffer
 * methods or fields annotated with {@link CasualFieldElement} will be handled
 * @see CasualFieldElement
 */
public final class FieldedTypeBufferProcessor
{
    private FieldedTypeBufferProcessor()
    {}

    /**
     * Marshall an object according to {@code FieldedTypeBufferProcessorMode.RELAXED}
     * @see FieldedTypeBufferProcessorMode
     * @param o the object
     * @return a new buffer
     */
    public static FieldedTypeBuffer marshall(final Object o)
    {
        return marshall(o, FieldedTypeBufferProcessorMode.RELAXED);
    }

    /**
     * Marshall an object according to mode
     * @see FieldedTypeBufferProcessorMode
     * @param o the object
     * @param mode the mode
     * @return a new buffer
     */
    public static FieldedTypeBuffer marshall(final Object o, FieldedTypeBufferProcessorMode mode)
    {
        Objects.requireNonNull(o, "object to be marshalled is not allowed to be null");
        FieldedTypeBuffer b = FieldedTypeBuffer.create();
        return Marshaller.write(o, b, mode);
    }

    /**
     * Unmarshall a buffer into {@code clazz} according to {@code FieldedTypeBufferProcessorMode.RELAXED}
     * @see FieldedTypeBufferProcessorMode
     * @param b the buffer
     * @param clazz the class of the object to be created
     * @param <T> the type of the class
     * @return a new instance of type T
     */
    public static <T> T unmarshall(final FieldedTypeBuffer b, final Class<T> clazz)
    {
        return unmarshall(b, clazz, FieldedTypeBufferProcessorMode.RELAXED);
    }

    /**
     * Unmarshall a buffer into {@code clazz} according to mode
     * @see FieldedTypeBufferProcessorMode
     * @param b the buffer
     * @param clazz the class of the object to be created
     * @param <T> the type of the class
     * @param mode the mode to use
     * @return a new instance of type T
     */
    public static <T> T unmarshall(final FieldedTypeBuffer b, final Class<T> clazz, FieldedTypeBufferProcessorMode mode)
    {
        Objects.requireNonNull(b, "fielded type buffer is not allowed to be null");
        Objects.requireNonNull(clazz, "clazz is not allowed to be null");
        // we make a copy of the buffer since operations that we will use are destructive
        // note: this is not very expensive since we use the same immutable references
        FieldedTypeBuffer copy = FieldedTypeBuffer.of(b);
        return Unmarshaller.createObject(copy, clazz, mode);
    }

    /**
     * Unmarshall the parameters for a method according to {@code FieldedTypeBufferProcessorMode.RELAXED}
     * @see FieldedTypeBufferProcessorMode
     * @param b the buffer
     * @param m the method
     * @return an object array containing the unmarshalled method parameters
     */
    public static Object[] unmarshall(final FieldedTypeBuffer b, final Method m)
    {
        return unmarshall(b, m, FieldedTypeBufferProcessorMode.RELAXED);
    }

    /**
     * Unmarshall the parameters for a method according to mode
     * @see FieldedTypeBufferProcessorMode
     * @param b the buffer
     * @param m the method
     * @param mode the mode to use
     * @return an object array containing the unmarshalled method parameters
     */
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
