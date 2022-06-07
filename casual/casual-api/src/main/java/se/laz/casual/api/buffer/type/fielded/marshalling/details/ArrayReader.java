/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import se.laz.casual.api.buffer.type.fielded.FieldedData;
import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import static se.laz.casual.api.buffer.type.fielded.marshalling.details.Unmarshaller.createObject;
import static se.laz.casual.api.buffer.type.fielded.marshalling.details.Unmarshaller.readAccordingToMode;
import static se.laz.casual.api.buffer.type.fielded.marshalling.details.Unmarshaller.toObject;

public final class ArrayReader
{
    private ArrayReader()
    {}

    static void readValue(UnmarshallerContext<?> context, CasualFieldElement annotation, Consumer<Object> consumer, Class<?> type, Function<Object, ?> mapper)
    {
        String listLengthName = CommonDetails.getListLengthName(annotation).orElse(null);
        if(null != listLengthName)
        {
            readBounded(context, listLengthName, annotation, consumer, type, mapper);
            return;
        }
        readUnbounded(context, annotation, consumer, type, mapper);
    }

    private static void readBounded(UnmarshallerContext<?> context, String listLengthName, CasualFieldElement annotation, Consumer<Object> consumer, Class<?> type, Function<Object,?> mapper)
    {
        int arraySize =  (int) toObject(context.getBuffer().read(listLengthName, 0, true), true);
        if (0 == arraySize)
        {
            consumer.accept(null);
        }
        Class<?> componentType = CommonDetails.wrapIfPrimitive(type.getComponentType());
        boolean castToInt = componentType.equals(Integer.class);
        Object array = Array.newInstance(type.getComponentType(), arraySize);
        if(type.getComponentType().isPrimitive() || CommonDetails.isFieldedType(componentType))
        {
            for (int i = 0; i < arraySize; ++i)
            {
                final int finalInt = i;
                Optional<FieldedData<?>> v = readAccordingToMode(context.getBuffer(), context.getMode(), annotation.name());
                v.ifPresent(r -> Array.set(array, finalInt, toObject(r, castToInt)));
            }
        }
        else
        {
            for (int i = 0; i < arraySize; ++i, context.increaseIndex())
            {
                readPOJO(context, array, componentType, mapper, annotation.name(), castToInt);
            }
        }
        consumer.accept(array);
    }

    private static void readUnbounded(UnmarshallerContext<?> context, CasualFieldElement annotation, Consumer<Object> consumer, Class<?> type, Function<Object,?> mapper)
    {
        Class<?> componentType = CommonDetails.wrapIfPrimitive(type.getComponentType());
        boolean castToInt = componentType.equals(Integer.class);
        List<Object> result = new ArrayList<>();
        if(type.getComponentType().isPrimitive() || CommonDetails.isFieldedType(componentType))
        {
            readPrimitiveOrFieldedValues(context, annotation.name(), result, castToInt);
        }
        else
        {
            readObjectValues(context, annotation.name(), result, castToInt, componentType, mapper);

        }
        consumer.accept(objectsToArray(result, type.getComponentType()));
    }

    private static void readObjectValues(UnmarshallerContext<?> context, String name, List<Object> result, boolean castToInt, Class<?> componentType, Function<Object, ?> mapper)
    {
        AtomicBoolean hasMoreObjects = new AtomicBoolean(true);
        while (hasMoreObjects.get())
        {
            if(null != mapper)
            {
                Optional<FieldedData<?>> v = readAccordingToMode(context.getBuffer(), context.getMode(), name);
                v.ifPresent(value -> result.add(mapper.apply(toObject(value, castToInt))));
                hasMoreObjects.set(v.isPresent());
            }
            else
            {
                UnmarshallerContext<?> newContext = UnmarshallerContextImpl.of(context,componentType, 0);
                Optional<?> object = createObject(newContext);
                object.ifPresent(result::add);
                hasMoreObjects.set(object.isPresent());
            }
        }
    }

    private static void readPrimitiveOrFieldedValues(UnmarshallerContext<?> context, String name, List<Object> result, boolean castToInt)
    {
        AtomicBoolean hasMoreObjects = new AtomicBoolean(true);
        while (hasMoreObjects.get())
        {
            Optional<FieldedData<?>> v = readAccordingToMode(context.getBuffer(), context.getMode(), name);
            v.ifPresent(r -> result.add(toObject(r, castToInt)));
            hasMoreObjects.set(v.isPresent());
        }
    }

    private static Object objectsToArray(List<Object> result, Class<?> componentType)
    {
        if(result.isEmpty())
        {
            return null;
        }
        Object objectArray = Array.newInstance(componentType, result.size());
        for(int i = 0; i < result.size(); ++i)
        {
            Array.set(objectArray, i , result.get(i));
        }
        return objectArray;
    }

    private static void readPOJO(UnmarshallerContext<?> context, final Object array, final Class<?> componentType, final Function<Object, ?> mapper, String name, boolean castToInt)
    {
        if(null != mapper)
        {
            Optional<FieldedData<?>> v = readAccordingToMode(context.getBuffer(), context.getMode(), name);
            v.ifPresent(value -> Array.set(array, context.getIndex(), mapper.apply(toObject(value, castToInt))));
        }
        else
        {
            Array.set(array, context.getIndex(), createObject(context.getBuffer(), componentType, context.getMode()));
        }
    }

}
