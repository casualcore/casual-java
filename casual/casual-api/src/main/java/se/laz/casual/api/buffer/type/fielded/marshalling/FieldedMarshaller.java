/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.buffer.type.fielded.marshalling;

import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.spi.Prioritisable;

import java.lang.reflect.Method;

public interface FieldedMarshaller extends Prioritisable
{
    /**
     * Marshall an object according to {@code FieldedTypeBufferProcessorMode.RELAXED}
     * @see FieldedTypeBufferProcessorMode
     * @param o the object
     * @return a new buffer
     */
     FieldedTypeBuffer marshall(final Object o);

    /**
     * Marshall an object according to mode
     * @see FieldedTypeBufferProcessorMode
     * @param o the object
     * @param mode the mode
     * @return a new buffer
     */
    FieldedTypeBuffer marshall(final Object o, FieldedTypeBufferProcessorMode mode);

    /**
     * Unmarshall a buffer into {@code clazz} according to {@code FieldedTypeBufferProcessorMode.RELAXED}
     * @see FieldedTypeBufferProcessorMode
     * @param b the buffer
     * @param clazz the class of the object to be created
     * @param <T> the type of the class
     * @return a new instance of type T
     */
    <T> T unmarshall(final FieldedTypeBuffer b, final Class<T> clazz);

    /**
     * Unmarshall a buffer into {@code clazz} according to mode
     * @see FieldedTypeBufferProcessorMode
     * @param b the buffer
     * @param clazz the class of the object to be created
     * @param <T> the type of the class
     * @param mode the mode to use
     * @return a new instance of type T
     */
     <T> T unmarshall(final FieldedTypeBuffer b, final Class<T> clazz, FieldedTypeBufferProcessorMode mode);

    /**
     * Unmarshall the parameters for a method according to {@code FieldedTypeBufferProcessorMode.RELAXED}
     * @see FieldedTypeBufferProcessorMode
     * @param b the buffer
     * @param m the method
     * @return an object array containing the unmarshalled method parameters
     */
    Object[] unmarshall(final FieldedTypeBuffer b, final Method m);

    /**
     * Unmarshall the parameters for a method according to mode
     * @see FieldedTypeBufferProcessorMode
     * @param b the buffer
     * @param m the method
     * @param mode the mode to use
     * @return an object array containing the unmarshalled method parameters
     */
    Object[] unmarshall(final FieldedTypeBuffer b, final Method m, FieldedTypeBufferProcessorMode mode);
}
