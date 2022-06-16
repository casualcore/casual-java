/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import se.laz.casual.api.buffer.type.fielded.FieldedData;
import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static se.laz.casual.api.buffer.type.fielded.marshalling.details.Unmarshaller.createObject;
import static se.laz.casual.api.buffer.type.fielded.marshalling.details.Unmarshaller.readAccordingToMode;
import static se.laz.casual.api.buffer.type.fielded.marshalling.details.Unmarshaller.toObject;

public final class ListReader
{
    private ListReader()
    {}

    static void read(UnmarshallerContext<?> context, CasualFieldElement annotation, Consumer<Object> consumer, Supplier<Type> listComponentType, Function<Object, ?> mapper)
    {
        Type listType = listComponentType.get();
        if(!(listType instanceof ParameterizedType))
        {
            throw new FieldedUnmarshallingException("list but type is not parameterized");
        }
        String listLengthName = CommonDetails.getListLengthName(annotation).orElse(null);
        if(null != listLengthName)
        {
            readBounded(context, listType, listLengthName, annotation, consumer, mapper);
            return;
        }
        readUnbounded(context , listType, annotation, consumer, mapper);
    }

    private static void readUnbounded(UnmarshallerContext<?> context, Type listType, CasualFieldElement annotation, Consumer<Object> consumer, Function<Object,?> mapper)
    {
        ParameterizedType parameterizedType = (ParameterizedType)listType;
        Type elementType = parameterizedType.getActualTypeArguments()[0];
        Class<?> elementClass = CommonDetails.adaptTypeToFielded(CommonDetails.wrapIfPrimitive(getClassForName(elementType.getTypeName())));
        List<Object> l = new ArrayList<>();
        final boolean castToInt = elementType.equals(Integer.class);
        if(CommonDetails.isFieldedType(elementClass))
        {
            boolean hasMoreElements = true;
            while(hasMoreElements)
            {
                Optional<FieldedData<?>> v = readAccordingToMode(context.getBuffer(), context.getMode(), annotation.name());
                v.ifPresent(r -> l.add(toObject(r, castToInt)));
                hasMoreElements = v.isPresent();
            }
        }
        else
        {
            boolean hasMoreElements = true;
            while(hasMoreElements)
            {
                hasMoreElements = readPojo(context, annotation.name(), l, mapper, elementClass, castToInt);
            }
        }
        consumer.accept(l);
    }

    private static void readBounded(UnmarshallerContext<?> context, Type listType, String listLengthName, CasualFieldElement annotation, Consumer<Object> consumer, Function<Object,?> mapper)
    {
        ParameterizedType parameterizedType = (ParameterizedType)listType;
        Type elementType = parameterizedType.getActualTypeArguments()[0];
        Class<?> elementClass = CommonDetails.adaptTypeToFielded(CommonDetails.wrapIfPrimitive(getClassForName(elementType.getTypeName())));
        int listSize = (int)toObject(context.getBuffer().read(listLengthName, 0, true), true);
        List<Object> l = new ArrayList<>(listSize);
        final boolean castToInt = elementType.equals(Integer.class);
        if(CommonDetails.isFieldedType(elementClass))
        {
            for(int i = 0; i < listSize; ++i)
            {
                Optional<FieldedData<?>> v = readAccordingToMode(context.getBuffer(), context.getMode(), annotation.name());
                v.ifPresent(r -> l.add(toObject(r, castToInt)));
            }
        }
        else
        {
            for(int i = 0; i < listSize; ++i)
            {
                readPojo(context, annotation.name(), l, mapper, elementClass, castToInt);
            }
        }
        consumer.accept(l);
    }

    private static Class<?> getClassForName(String name)
    {
        try
        {
            return Class.forName(name);
        }
        catch (ClassNotFoundException e)
        {
            throw new FieldedUnmarshallingException("No class found by name: " + name, e);
        }
    }

    private static boolean readPojo(final UnmarshallerContext<?> context, String name, List<Object> l, Function<Object, ?> mapper, Class<?> elementClass, boolean castToInt)
    {
        AtomicBoolean didRead = new AtomicBoolean(false);
        if(null != mapper)
        {
            Optional<FieldedData<?>> v = readAccordingToMode(context.getBuffer(), context.getMode(), name);
            v.ifPresent(value -> l.add(mapper.apply(toObject(value, castToInt))));
            didRead.set(v.isPresent());
        }
        else
        {
            UnmarshallerContext<?> newContext = UnmarshallerContextImpl.of(context, elementClass, 0);
            Object v = createObject(newContext).orElseGet(() -> null);
            if (null != v)
            {
                l.add(v);
                didRead.set(true);
            }
        }
        return didRead.get();
    }
}
