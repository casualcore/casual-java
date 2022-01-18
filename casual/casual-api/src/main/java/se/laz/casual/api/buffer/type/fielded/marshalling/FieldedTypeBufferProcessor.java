/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling;

import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.laz.casual.spi.Prioritise;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * marshall/unmarshall FieldedTypeBuffer
 * methods or fields annotated with {@link CasualFieldElement} will be handled
 * @see CasualFieldElement
 *
 * Uses SPI to load the {@link FieldedMarshaller} with the highest {@link se.laz.casual.spi.Priority}
 */
public final class FieldedTypeBufferProcessor
{
    private static ServiceLoader<FieldedMarshaller> marshallerServiceLoader = ServiceLoader.load( FieldedMarshaller.class );
    private static FieldedMarshaller marshaller = loadMarshaller();
    private FieldedTypeBufferProcessor()
    {}

    private static FieldedMarshaller loadMarshaller()
    {
        List<FieldedMarshaller> marshallers = new ArrayList<>();
        for ( FieldedMarshaller m : marshallerServiceLoader)
        {
            marshallers.add(m);
        }
        Prioritise.highestToLowest( marshallers );
        Optional<FieldedMarshaller> marshaller = Optional.ofNullable( marshallers.isEmpty() ? null : marshallers.get(0));
        return marshaller.orElseThrow(() -> new NoFieldedMarshallerException("No marshaller available"));
    }

    /**
     * Marshall an object according to {@code FieldedTypeBufferProcessorMode.RELAXED}
     * @see FieldedTypeBufferProcessorMode
     * @param o the object
     * @return a new buffer
     */
    public static FieldedTypeBuffer marshall(final Object o)
    {
        return marshaller.marshall(o, FieldedTypeBufferProcessorMode.RELAXED);
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
        return marshaller.marshall(o, mode);
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
        return marshaller.unmarshall(b, clazz, FieldedTypeBufferProcessorMode.RELAXED);
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
        return marshaller.unmarshall(b, clazz, mode);
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
        return marshaller.unmarshall(b, m, FieldedTypeBufferProcessorMode.RELAXED);
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
        return marshaller.unmarshall(b, m, mode);
    }

}
