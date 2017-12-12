package se.kodarkatten.casual.api.buffer.type.fielded.marshalling.details;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisSerializer;
import org.objenesis.instantiator.ObjectInstantiator;
import se.kodarkatten.casual.api.buffer.type.fielded.FieldedData;
import se.kodarkatten.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;

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
import java.util.function.Supplier;
import java.util.stream.Collectors;

// sonar hates lambdas
@SuppressWarnings("squid:S1612")
public final class Unmarshaller
{
    private Unmarshaller()
    {}

    public static <T> T createObject(final FieldedTypeBuffer b, final Class<T> clazz)
    {
        return createObject(b, clazz, 0).orElseThrow(() -> new FieldedUnmarshallingException("could not create instance of "+ clazz + " with buffer: " + b));
    }

    private static <T> Optional<T> createObject(final FieldedTypeBuffer b, final Class<T> clazz, int listIndex)
    {
        Objenesis objenesis = new ObjenesisSerializer();
        ObjectInstantiator<T> clazzInstantiator = objenesis.getInstantiatorOf(clazz);
        T instance = clazzInstantiator.newInstance();
        List<Field> fields = CommonDetails.getCasuallyAnnotatedFields(instance);
        try
        {
            boolean didReadField = readFields(b, instance, fields, listIndex);
            Map<Method, List<AnnotatedParameterInfo>> methodInfo = CommonDetails.getCasuallyAnnotatedParameters(instance);
            boolean didReadAnnotatedParam = readMethodsWithAnnotatedParameters(b, instance, methodInfo, listIndex);
            return (didReadField || didReadAnnotatedParam) ? Optional.of(instance) : Optional.empty();
        }
        catch (ClassNotFoundException e)
        {
            throw new FieldedUnmarshallingException(e);
        }
    }

    private static <T> boolean readMethodsWithAnnotatedParameters(FieldedTypeBuffer b, T instance, Map<Method, List<AnnotatedParameterInfo>> methodInfo, int listIndex) throws ClassNotFoundException
    {
        boolean readFieldedParam = false;
        for(Map.Entry<Method, List<AnnotatedParameterInfo>> m : methodInfo.entrySet())
        {
            // parameter info is in parameter order
            Object[] instantiatedMethodParameters = instantiateMethodParameters(b, m.getValue(), listIndex);
            try
            {
                if(instantiatedMethodParameters.length > 0)
                {
                    m.getKey().invoke(instance, instantiatedMethodParameters);
                    readFieldedParam = true;
                }
            }
            catch (IllegalAccessException |  InvocationTargetException e)
            {
                throw new FieldedUnmarshallingException("could not invoke method: " + m + "\nmethod params: " + instantiatedMethodParameters + "\n parameter info:" + methodInfo, e);
            }
        }
        return readFieldedParam;
    }

    private static Object[] instantiateMethodParameters(FieldedTypeBuffer b, List<AnnotatedParameterInfo> parameterInfo, int index) throws ClassNotFoundException
    {
        // we need to instantiate each parameter
        Object[] l = new Object[parameterInfo.size()];
        for(int i = 0; i < parameterInfo.size(); ++i)
        {
            final int finalIndex = i;
            final AnnotatedParameterInfo info = parameterInfo.get(i);
            readValue(b, info.getAnnotation(), (Object v) -> l[finalIndex] = v, info.getType(), () -> info.getParameterizedType(), index);
        }
        return l;
    }

    private static <T> boolean readFields(FieldedTypeBuffer b, T instance, List<Field> fields, int index) throws ClassNotFoundException
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
                readValue(b, annotation, (Object v) -> {
                    try
                    {
                        f.set(instance, v);
                        fieldedValueUnmarshalled.set(true);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new FieldedUnmarshallingException(e);
                    }
                },f.getType(), () -> f.getGenericType(), index);
            }
            finally
            {
                f.setAccessible(fieldAccessible);
            }
        }
        return fieldedValueUnmarshalled.get();
    }

    public static void readValue(FieldedTypeBuffer b, CasualFieldElement annotation, Consumer<Object> consumer, Class<?> type, Supplier<Type> listComponentType, int listIndex) throws ClassNotFoundException
    {
        Class<?> wrappedType = CommonDetails.wrapIfPrimitive(type);
        boolean castToInt = wrappedType.equals(Integer.class);
        if(CommonDetails.isListType(wrappedType))
        {
            readListValue(b, annotation, consumer, listComponentType);
        }
        else if(CommonDetails.isArrayType(wrappedType))
        {
            readArrayValue(b, annotation, consumer, type);
        }
        else if(CommonDetails.isFieldedType(wrappedType) || wrappedType.equals(Integer.class))
        {
            FieldedData<?> v = b.peek(annotation.name(), listIndex).orElseGet(() -> null);
            if(null != v)
            {
                consumer.accept(toObject(v, castToInt));
            }
        }
        else
        {
            // should be POJO type
            consumer.accept(createObject(b, type));
        }
    }

    public static void readArrayValue(FieldedTypeBuffer b, CasualFieldElement annotation, Consumer<Object> consumer, Class<?> type)
    {
        Class<?> componentType = CommonDetails.wrapIfPrimitive(type.getComponentType());
        boolean castToInt = componentType.equals(Integer.class);
        List<FieldedData<?>> fieldedData = b.readAll(annotation.name());
        Object array = Array.newInstance(type.getComponentType(), fieldedData.size());
        for(int i = 0; i < fieldedData.size(); ++i)
        {
            Array.set(array, i, toObject(fieldedData.get(i), castToInt));
        }
        consumer.accept(array);
    }

    public static void readListValue(FieldedTypeBuffer b, CasualFieldElement annotation, Consumer<Object> consumer, Supplier<Type> listComponentType) throws ClassNotFoundException
    {
        Type listType = listComponentType.get();
        if(!(listType instanceof ParameterizedType))
        {
            throw new FieldedUnmarshallingException("list but type is not parameterized");
        }
        ParameterizedType parameterizedType = (ParameterizedType)listType;
        Type elementType = parameterizedType.getActualTypeArguments()[0];
        Class<?> elementClass = CommonDetails.adaptTypeToFielded(CommonDetails.wrapIfPrimitive(Class.forName(elementType.getTypeName())));
        List<Object> l;
        if(CommonDetails.isFieldedType(elementClass))
        {
            final boolean castToInt = elementType.equals(Integer.class);
            l = b.readAll(annotation.name()).stream()
                              .map(v -> toObject(v, castToInt))
                              .collect(Collectors.toList());
        }
        else
        {
            l = new ArrayList<>();
            // we do it this way since we do not transport how many elements there are
            // thus we read until there are no more elements
            for(int i = 0; i < Integer.MAX_VALUE; ++i)
            {
                Object v = createObject(b, elementClass, i).orElseGet(() -> null);
                if(null != v)
                {
                    l.add(v);
                }
                else
                {
                    // done, no more such POJOs
                    break;
                }
            }
        }
        consumer.accept(l);
    }

    private static Object toObject(FieldedData<?> f, boolean castToInt)
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
