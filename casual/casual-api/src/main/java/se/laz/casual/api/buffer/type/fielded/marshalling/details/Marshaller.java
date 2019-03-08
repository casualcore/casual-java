/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedMarshallingException;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessorMode;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A marshaller implementation
 * Marshalls into a {@link FieldedTypeBuffer}
 */
public final class Marshaller
{
    private Marshaller()
    {}

    /**
     * Marshall object into a buffer
     * @param o the object
     * @param b the buffer
     * @param mode the mode
     * @return the buffer containing the marshalled values
     */
    public static FieldedTypeBuffer write(final Object o, FieldedTypeBuffer b, FieldedTypeBufferProcessorMode mode)
    {
        List<Field> fields = CommonDetails.getCasuallyAnnotatedFields(o.getClass());
        writeFields(o, fields, b, mode);
        List<Method> methods = CommonDetails.getCasuallyAnnotatedMethods(o.getClass());
        return writeMethodReturnValues(o, methods, b, mode);
    }

    private static FieldedTypeBuffer writeMethodReturnValues(Object o, List<Method> methods, FieldedTypeBuffer b, FieldedTypeBufferProcessorMode mode)
    {
        // For methods we get the return value and store it
        for(Method m : methods)
        {
            if(m.getParameterCount() > 0)
            {
                throw new FieldedMarshallingException("@CasualFieldElement annotated methods can not have parameters, method: " + m);
            }
            CasualFieldElement annotation = m.getAnnotation(CasualFieldElement.class);
            boolean access = m.isAccessible();
            if(!access)
            {
                m.setAccessible(true);
            }
            try
            {
                Object returnValue = m.invoke(o);
                writeBasedOnMode(returnValue, b, annotation, mode);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                throw new FieldedMarshallingException("can not marshall parameter annotations for method: " + m, e);
            }
            finally
            {
                m.setAccessible(access);
            }
        }
        return b;
    }

    private static FieldedTypeBuffer writeFields(final Object o, final List<Field> fields, FieldedTypeBuffer b, FieldedTypeBufferProcessorMode mode)
    {
        for(Field f : fields)
        {
            CasualFieldElement annotation = f.getAnnotation(CasualFieldElement.class);
            boolean access = f.isAccessible();
            if(!access)
            {
                f.setAccessible(true);
            }
            try
            {
                Object fieldValue = f.get(o);
                writeBasedOnMode(fieldValue, b, annotation, mode);
            }
            catch (IllegalAccessException e)
            {
                throw new FieldedMarshallingException("can not marshall field: " + f, e);
            }
            finally
            {
                f.setAccessible(access);
            }
        }
        return b;
    }

    private static void writeBasedOnMode(Object maybeValue, FieldedTypeBuffer b, CasualFieldElement annotation, FieldedTypeBufferProcessorMode mode)
    {
        if(FieldedTypeBufferProcessorMode.RELAXED == mode)
        {
            writeRelaxed(maybeValue, b, annotation, mode);
        }
        else
        {
            if(null == maybeValue)
            {
                throw new FieldedMarshallingException("strict mode but the value for @CasualFieldElement: " + annotation + " is null");
            }
            writeRelaxed(maybeValue, b, annotation, mode);
        }
    }

    private static void writeRelaxed(Object maybeValue, FieldedTypeBuffer b, CasualFieldElement annotation, FieldedTypeBufferProcessorMode mode)
    {
        if(null != maybeValue)
        {
            Object v = CommonDetails.wrapIfPrimitive(maybeValue.getClass()).cast(maybeValue);
            v = CommonDetails.adaptValueToFielded(v);
            writeValue(b, annotation, v, mode);
        }
    }

    public static void writeValue(final FieldedTypeBuffer b, final CasualFieldElement annotation,  final Object v, FieldedTypeBufferProcessorMode mode)
    {
        Optional<Function<Object, ? extends Object>> mapper = CommonDetails.getMapperTo(annotation);
        Class<?> clazz = v.getClass();
        if(CommonDetails.isListType(clazz))
        {
            List<?> l = (List<?>)v;
            writeListType(b, annotation, l, mode, mapper);
        }
        else if(CommonDetails.isArrayType(clazz))
        {
            writeArrayType(b, annotation, v, mode, mapper);
        }
        else if(CommonDetails.isFieldedType(clazz))
        {
            b.write(annotation.name(), v);
        }
        else
        {
            writePOJO(v, b, mode, mapper.orElseGet(() -> null), annotation.name());
        }
    }

    public static void writeArrayType(final FieldedTypeBuffer b, final CasualFieldElement annotation, final Object o, FieldedTypeBufferProcessorMode mode, Optional<Function<Object, ?>> mapper)
    {
        Class<?> componentType = o.getClass().getComponentType();
        int arrayLength = Array.getLength(o);
        String listLengthName = CommonDetails.getListLengthName(annotation).orElseThrow(() -> new FieldedUnmarshallingException("array type but @CasualFieldElement is missing lengthName!"));
        b.write(listLengthName, (long)arrayLength);
        if(componentType.isPrimitive() || CommonDetails.isFieldedType(componentType))
        {
            Object[] array = toObjectArray(o, componentType, arrayLength);
            for (Object v : array)
            {
                b.write(annotation.name(), CommonDetails.adaptValueToFielded(v));
            }
        }
        else
        {
            Object[] array = (Object[])o;
            for(Object v : array)
            {
                writePOJO(v, b, mode, mapper.orElseGet(() -> null), annotation.name());
            }
        }
    }

    private static void writePOJO(Object v, FieldedTypeBuffer b, FieldedTypeBufferProcessorMode mode, Function<Object, ?> mapper, String name)
    {
        Object mapped = (null == mapper) ? v : CommonDetails.adaptValueToFielded(mapper.apply(v));
        if(CommonDetails.isFieldedType(mapped.getClass()))
        {
            b.write(name, mapped);
        }
        else
        {
            write(v, b, mode);
        }
    }

    public static Object[] toObjectArray(Object array, Class<?> componentType, int arrayLength)
    {
        if (componentType.isPrimitive())
        {
            Object[] r = new Object[arrayLength];
            for (int i = 0; i < arrayLength; ++i)
            {
                r[i] = Array.get(array, i);
            }
            return r;
        }
        return (Object[]) array;
    }

    public static void writeListType(final FieldedTypeBuffer b, final CasualFieldElement annotation, final List<?> l, FieldedTypeBufferProcessorMode mode, Optional<Function<Object, ?>> mapper)
    {
        String listLengthName = CommonDetails.getListLengthName(annotation).orElseThrow(() -> new FieldedUnmarshallingException("list type but @CasualFieldElement is missing lengthName!"));
        b.write(listLengthName, (long)l.size());
        if(l.isEmpty())
        {
            return;
        }
        if(CommonDetails.isFieldedType(CommonDetails.wrapIfPrimitive(CommonDetails.adaptValueToFielded(l.get(0)).getClass())))
        {
            for (Object v : l)
            {
                b.write(annotation.name(), CommonDetails.adaptValueToFielded(v));
            }
        }
        else
        {
            for(Object v: l)
            {
                writePOJO(v, b, mode, mapper.orElseGet(() -> null), annotation.name());
            }
        }
    }

}
