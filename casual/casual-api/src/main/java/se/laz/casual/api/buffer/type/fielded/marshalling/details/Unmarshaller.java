/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import se.laz.casual.api.buffer.type.fielded.FieldedData;
import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessorMode;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;
import se.laz.casual.api.util.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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
        UnmarshallerContext<T> context = UnmarshallerContextImpl.of(b, 0, mode, clazz);
        return createObject(context).orElseThrow(() -> new FieldedUnmarshallingException("could not create instance using context: " + context));
    }

    public static Object[] createMethodParameterObjects(FieldedTypeBuffer b, Method m, FieldedTypeBufferProcessorMode mode)
    {
        UnmarshallerContext<?> context = UnmarshallerContextImpl.of(b, 0, mode, m);
        return instantiateMethodParameters(context);
    }

    static <T> Optional<T> createObject(UnmarshallerContext<T> context)
    {
        boolean didReadField = readFields(context);
        boolean didReadAnnotatedParam = readMethodsWithAnnotatedParameters(context);
        return (didReadField || didReadAnnotatedParam) ? Optional.of(context.getInstance()) : Optional.empty();
    }

    @SuppressWarnings("squid:S1452")
    static Optional<FieldedData<?>> readAccordingToMode(FieldedTypeBuffer buffer,FieldedTypeBufferProcessorMode mode, String name)
    {
        Optional<FieldedData<?>> v = buffer.peek(name, 0, true);
        if(!v.isPresent() && FieldedTypeBufferProcessorMode.STRICT == mode)
        {
            throw new FieldedUnmarshallingException("strict mode and missing value for name: " + name);
        }
        return v;
    }

    static Object toObject(final FieldedData<?> f, boolean castToInt)
    {
        // since int is transported as long
        Object v = f.getData();
        if(castToInt)
        {
            v = Math.toIntExact((long)v);
        }
        return v;
    }

    private static <T> boolean readMethodsWithAnnotatedParameters(UnmarshallerContext<T> context)
    {
        boolean readFieldedParam = false;
        for(Map.Entry<Method, List<ParameterInfo>> m : context.getMethodInfo().entrySet())
        {
            Object[] instantiatedMethodParameters;
            try
            {
                // parameter info is in parameter order
                UnmarshallerContext<?> newContext = UnmarshallerContextImpl.of(context, m.getKey(), context.getIndex());
                instantiatedMethodParameters = instantiateMethodParameters(newContext);
                if(instantiatedMethodParameters.length > 0)
                {
                    m.getKey().invoke(context.getInstance(), instantiatedMethodParameters);
                    readFieldedParam = true;
                }
            }
            catch (IllegalAccessException |  InvocationTargetException e)
            {
                throw new FieldedUnmarshallingException("could not invoke method: " + m + "\n context:" + context, e);
            }
        }
        return readFieldedParam;
    }

    private static Object[] instantiateMethodParameters(UnmarshallerContext<?> context)
    {
        // we need to instantiate each parameter
        List<ParameterInfo> parameterInfo = context.getParameterInfo();
        Object[] l = new Object[parameterInfo.size()];
        for(int i = 0; i < parameterInfo.size(); ++i)
        {
            final int finalIndex = i;
            final ParameterInfo p = parameterInfo.get(i);
            if(p instanceof AnnotatedParameterInfo)
            {
                AnnotatedParameterInfo info = (AnnotatedParameterInfo)p;
                readValue(context, info.getAnnotation(), (Object v) -> l[finalIndex] = v, info.getType(), info::getParameterizedType);
            }
            else
            {
                l[i] = createObject(context.getBuffer(), p.getType(), context.getMode());
            }
        }
        return l;
    }

    private static <T> boolean readFields(UnmarshallerContext<T> context)
    {
        AtomicBoolean fieldedValueUnmarshalled = new AtomicBoolean(false);
        List<Field> fields = context.getFields();
        for(Field f : fields)
        {
            CasualFieldElement annotation = f.getAnnotation(CasualFieldElement.class);
            f.setAccessible(true);
            readValue(context, annotation, (Object v) -> setField(context.getInstance(), f, v, fieldedValueUnmarshalled),f.getType(), f::getGenericType);
        }
        return fieldedValueUnmarshalled.get();
    }

    static void readValue(UnmarshallerContext<?> context, CasualFieldElement annotation, Consumer<Object> consumer, Class<?> type, Supplier<Type> listComponentType)
    {
        Optional<Pair<Function<Object, ?>, Class<?>>> mappingInfo = CommonDetails.getMapperFrom(annotation);
        final Optional<Function<Object, ? extends Object>> mapper = mappingInfo.isPresent() ? Optional.of(mappingInfo.get().first()) : Optional.empty();
        Class<?> wrappedType = CommonDetails.wrapIfPrimitive(type);
        if(CommonDetails.isListType(wrappedType))
        {
            ListReader.read(context, annotation, consumer, listComponentType, mapper.orElse(null));
        }
        else if(CommonDetails.isArrayType(wrappedType))
        {
            ArrayReader.readValue(context, annotation, consumer, type, mapper.orElse(null));
        }
        else if(CommonDetails.isFieldedType(wrappedType) || wrappedType.equals(Integer.class))
        {
            boolean castToInt = wrappedType.equals(Integer.class);
            Optional<FieldedData<?>> v = readAccordingToMode(context.getBuffer(), context.getMode(), annotation.name());
            v.ifPresent(value -> consumer.accept(mapValue(toObject(value, castToInt), mapper.orElseGet(() -> null))));
        }
        else
        {
            Class<?> mapperReturnType = mappingInfo.isPresent() ? mappingInfo.get().second() : null;
            if(null != mapperReturnType)
            {
                readValue(context, annotation, consumer, mapperReturnType, listComponentType);
            }
            else
            {
                // should be a fielded POJO type
                UnmarshallerContext<?> newContext = UnmarshallerContextImpl.of(context, type, 0);
                Optional<?> maybeObject = createObject(newContext);
                maybeObject.ifPresent(consumer::accept);
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

}
