/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisSerializer;
import org.objenesis.instantiator.ObjectInstantiator;
import se.laz.casual.api.buffer.type.fielded.FieldedData;
import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessorMode;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;
import se.laz.casual.api.util.Pair;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Unmarshalls from a buffer to an Object of type T
 */
// sonar hates lambdas
@SuppressWarnings("squid:S1612")
public final class Unmarshaller
{
    private Unmarshaller()
    {}

    /**
     * Unmarshalls from the buffer to an Object of type T
     * @param b the buffer
     * @param clazz the class
     * @param mode the mode
     * @param <T> the type
     * @return an instance of type T
     */
    public static <T> T createObject(final FieldedTypeBuffer b, final Class<T> clazz, FieldedTypeBufferProcessorMode mode)
    {
        return createObject(b, clazz, 0, mode).orElseThrow(() -> new FieldedUnmarshallingException("could not create instance of "+ clazz + " with buffer: " + b));
    }

    private static <T> Optional<T> createObject(final FieldedTypeBuffer b, final Class<T> clazz, int listIndex, FieldedTypeBufferProcessorMode mode)
    {
        Objenesis objenesis = new ObjenesisSerializer();
        ObjectInstantiator<T> clazzInstantiator = objenesis.getInstantiatorOf(clazz);
        T instance = clazzInstantiator.newInstance();
        List<Field> fields = CommonDetails.getCasuallyAnnotatedFields(instance.getClass());
        boolean didReadField = readFields(b, instance, fields, listIndex, mode);
        Map<Method, List<ParameterInfo>> methodInfo = CommonDetails.getParameterInfo(instance.getClass());
        boolean didReadAnnotatedParam = readMethodsWithAnnotatedParameters(b, instance, methodInfo, listIndex, mode);
        return (didReadField || didReadAnnotatedParam) ? Optional.of(instance) : Optional.empty();
    }

    public static Object[] createMethodParameterObjects(FieldedTypeBuffer b, Method m, FieldedTypeBufferProcessorMode mode)
    {
        List<ParameterInfo> parameterInfo = CommonDetails.getParameterInfo(m);
        try
        {
            return instantiateMethodParameters(b, parameterInfo, 0 , mode);
        }
        catch (ClassNotFoundException e)
        {
            throw new FieldedUnmarshallingException("could not create parameter objects for method:" + m + " with buffer: " + b + " using mode: " + mode, e);
        }
    }

    private static <T> boolean readMethodsWithAnnotatedParameters(FieldedTypeBuffer b, T instance, Map<Method, List<ParameterInfo>> methodInfo, int listIndex, FieldedTypeBufferProcessorMode mode)
    {
        boolean readFieldedParam = false;
        for(Map.Entry<Method, List<ParameterInfo>> m : methodInfo.entrySet())
        {
            Object[] instantiatedMethodParameters;
            try
            {
                // parameter info is in parameter order
                instantiatedMethodParameters = instantiateMethodParameters(b, m.getValue(), listIndex, mode);
                if(instantiatedMethodParameters.length > 0)
                {
                    m.getKey().invoke(instance, instantiatedMethodParameters);
                    readFieldedParam = true;
                }
            }
            catch (IllegalAccessException |  InvocationTargetException | ClassNotFoundException e)
            {
                throw new FieldedUnmarshallingException("could not invoke method: " + m + "\n parameter info:" + methodInfo, e);
            }
        }
        return readFieldedParam;
    }

    private static Object[] instantiateMethodParameters(FieldedTypeBuffer b, List<ParameterInfo> parameterInfo, int index, FieldedTypeBufferProcessorMode mode) throws ClassNotFoundException
    {
        // we need to instantiate each parameter
        Object[] l = new Object[parameterInfo.size()];
        for(int i = 0; i < parameterInfo.size(); ++i)
        {
            final int finalIndex = i;
            final ParameterInfo p = parameterInfo.get(i);
            if(p instanceof AnnotatedParameterInfo)
            {
                AnnotatedParameterInfo info = (AnnotatedParameterInfo)p;
                readValue(b, info.getAnnotation(), (Object v) -> l[finalIndex] = v, info.getType(), () -> info.getParameterizedType(), index, mode);
            }
            else
            {
                l[i] = createObject(b, p.getType(), mode);
            }
        }
        return l;
    }

    private static <T> boolean readFields(FieldedTypeBuffer b, T instance, List<Field> fields, int index, FieldedTypeBufferProcessorMode mode)
    {
        AtomicBoolean fieldedValueUnmarshalled = new AtomicBoolean(false);
        for(Field f : fields)
        {
            CasualFieldElement annotation = f.getAnnotation(CasualFieldElement.class);
            boolean fieldAccessible = f.isAccessible();
            if(!fieldAccessible)
            {
                f.setAccessible(true);
            }
            try
            {
                readValue(b, annotation, (Object v) -> setField(instance, f, v, fieldedValueUnmarshalled),f.getType(), () -> f.getGenericType(), index, mode);
            }
            catch (ClassNotFoundException e)
            {
                throw new FieldedUnmarshallingException(e);
            }
            finally
            {
                f.setAccessible(fieldAccessible);
            }
        }
        return fieldedValueUnmarshalled.get();
    }

    public static void readValue(FieldedTypeBuffer b, CasualFieldElement annotation, Consumer<Object> consumer, Class<?> type, Supplier<Type> listComponentType, int listIndex, FieldedTypeBufferProcessorMode mode) throws ClassNotFoundException
    {
        Optional<Pair<Function<Object, ?>, Class<?>>> mappingInfo = CommonDetails.getMapperFrom(annotation);
        Class<?> mapperReturnType = mappingInfo.isPresent() ? mappingInfo.get().second() : null;
        final Optional<Function<Object, ? extends Object>> mapper = mappingInfo.isPresent() ? Optional.of(mappingInfo.get().first()) : Optional.empty();
        Class<?> wrappedType = CommonDetails.wrapIfPrimitive(type);
        boolean castToInt = wrappedType.equals(Integer.class);
        if(CommonDetails.isListType(wrappedType))
        {
            readListValue(b, annotation, consumer, listComponentType, mode, mapper);
        }
        else if(CommonDetails.isArrayType(wrappedType))
        {
            readArrayValue(b, annotation, consumer, type, mode, mapper);
        }
        else if(CommonDetails.isFieldedType(wrappedType) || wrappedType.equals(Integer.class))
        {
            Optional<FieldedData<?>> v = readAccordingToMode(b, annotation.name(), listIndex, mode);
            v.ifPresent(value -> consumer.accept(mapValue(toObject(value, castToInt), mapper.orElseGet(() -> null))));
        }
        else
        {
            if(null != mapperReturnType)
            {
                readValue(b, annotation, consumer, mapperReturnType, listComponentType, listIndex, mode);
            }
            else
            {
                // should be a fielded POJO type
                consumer.accept(createObject(b, type, mode));
            }
        }
    }

    private static void setField(final Object instance, final Field f, final Object v, final AtomicBoolean fieldedValueUnmarshalled )
    {
        try
        {
            f.set(instance, v);
            fieldedValueUnmarshalled.set(true);
        }
        catch (IllegalAccessException e)
        {
            throw new FieldedUnmarshallingException(e);
        }
    }

    private static Object mapValue(Object o, Function<Object, ? extends Object> mapper)
    {
        return (null == mapper) ? o : mapper.apply(o);
    }

    private static Optional<FieldedData<?>> readAccordingToMode(FieldedTypeBuffer b, String name, int index, FieldedTypeBufferProcessorMode mode)
    {
        Optional<FieldedData<?>> v = b.peek(name, index, true);
        if(!v.isPresent() &&FieldedTypeBufferProcessorMode.STRICT == mode)
        {
            throw new FieldedUnmarshallingException("strict mode and missing value for name: " + name + " with index: " + index);
        }
        return v;
    }

    public static void readArrayValue(FieldedTypeBuffer b, CasualFieldElement annotation, Consumer<Object> consumer, Class<?> type, FieldedTypeBufferProcessorMode mode, Optional<Function<Object, ?>> mapper)
    {
        String listLengthName = CommonDetails.getListLengthName(annotation).orElseThrow(() -> new FieldedUnmarshallingException("list type but @CasualFieldElement is missing lengthName!"));
        int arraySize = (int)toObject(b.read(listLengthName, 0, true), true);
        if(0 == arraySize)
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
                Optional<FieldedData<?>> v = readAccordingToMode(b, annotation.name(), 0, mode);
                v.ifPresent(r -> Array.set(array, finalInt, toObject(r, castToInt)));
            }
        }
        else
        {
            for (int i = 0; i < arraySize; ++i)
            {
                readArrayPOJO(array, i, b, componentType, mode, mapper, annotation.name(), castToInt);
            }
        }
        consumer.accept(array);
    }

    // squid:S00107 - too many method arguments, we do need them all though
    @SuppressWarnings("squid:S00107")
    private static void readArrayPOJO(final Object array, int i, final FieldedTypeBuffer b, final Class<?> componentType, FieldedTypeBufferProcessorMode mode, final Optional<Function<Object, ?>> mapper, String name, boolean castToInt)
    {
        mapper.ifPresent(m -> {
            Optional<FieldedData<?>> v = readAccordingToMode(b, name, 0, mode);
            v.ifPresent(value -> Array.set(array, i, m.apply(toObject(value, castToInt))));
        });
        if(!mapper.isPresent())
        {
            Array.set(array, i, createObject(b, componentType, mode));
        }
    }

    public static void readListValue(FieldedTypeBuffer b, CasualFieldElement annotation, Consumer<Object> consumer, Supplier<Type> listComponentType, FieldedTypeBufferProcessorMode mode, Optional<Function<Object, ?>> mapper) throws ClassNotFoundException
    {
        Type listType = listComponentType.get();
        if(!(listType instanceof ParameterizedType))
        {
            throw new FieldedUnmarshallingException("list but type is not parameterized");
        }
        ParameterizedType parameterizedType = (ParameterizedType)listType;
        Type elementType = parameterizedType.getActualTypeArguments()[0];
        Class<?> elementClass = CommonDetails.adaptTypeToFielded(CommonDetails.wrapIfPrimitive(Class.forName(elementType.getTypeName())));
        String listLengthName = CommonDetails.getListLengthName(annotation).orElseThrow(() -> new FieldedUnmarshallingException("list type but @CasualFieldElement is missing lengthName!"));
        int listSize = (int)toObject(b.read(listLengthName, 0, true), true);
        List<Object> l = new ArrayList<>(listSize);
        final boolean castToInt = elementType.equals(Integer.class);
        if(CommonDetails.isFieldedType(elementClass))
        {
            for(int i = 0; i < listSize; ++i)
            {
                Optional<FieldedData<?>> v = readAccordingToMode(b, annotation.name(), 0, mode);
                v.ifPresent(r -> l.add(toObject(r, castToInt)));
            }
        }
        else
        {
            for(int i = 0; i < listSize; ++i)
            {
                readListPojo(b, annotation.name(), mode, l, mapper, elementClass, castToInt);
            }
        }
        consumer.accept(l);
    }

    private static void readListPojo(FieldedTypeBuffer b, String name, FieldedTypeBufferProcessorMode mode, List<Object> l, Optional<Function<Object, ?>> mapper, Class<?> elementClass, boolean castToInt)
    {
        mapper.ifPresent(m -> {
            Optional<FieldedData<?>> v = readAccordingToMode(b, name, 0, mode);
            v.ifPresent(value -> l.add(m.apply(toObject(value, castToInt))));
        });
        if(!mapper.isPresent())
        {
            Object v = createObject(b, elementClass, 0, mode).orElseGet(() -> null);
            if (null != v)
            {
                l.add(v);
            }
        }
    }


    private static Object toObject(final FieldedData<?> f, boolean castToInt)
    {
        // since int is transported as long
        Object v = f.getData();
        if(castToInt)
        {
            v = Math.toIntExact((long)v);
        }
        return v;
    }


}
