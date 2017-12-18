package se.kodarkatten.casual.api.buffer.type.fielded.marshalling.details;

import se.kodarkatten.casual.api.buffer.type.fielded.FieldType;
import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.kodarkatten.casual.api.buffer.type.fielded.mapper.CasualObjectMapper;
import se.kodarkatten.casual.api.buffer.type.fielded.mapper.PassThroughMapper;
import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.FieldedMarshallingException;
import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;
import se.kodarkatten.casual.api.util.FluentMap;
import se.kodarkatten.casual.api.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// sonar sucks understanding lambdas
@SuppressWarnings("squid:S1612")
public final class CommonDetails
{
    private static final Map<Class<?>, Class<?>> mapper;

    static
    {

        mapper = Collections.unmodifiableMap(FluentMap.of(new HashMap<Class<?>, Class<?>>())
                                                      .put(boolean.class, Boolean.class)
                                                      .put(byte.class, Byte.class)
                                                      .put(char.class, Character.class)
                                                      .put(double.class, Double.class)
                                                      .put(float.class, Float.class)
                                                      .put(int.class, Integer.class)
                                                      .put(long.class, Long.class)
                                                      .put(short.class, Short.class)
                                                      .put(void.class, Void.class)
                                                      .map());
    }


    private CommonDetails()
    {}

    public static List<Field> getCasuallyAnnotatedFields(final Class<?> c)
    {
        return Arrays.stream(c.getDeclaredFields())
                     .filter(f -> hasCasualFieldAnnotation(f))
                     .collect(Collectors.toList());
    }


    public static List<Method> getCasuallyAnnotatedMethods(final Class<?> c)
    {
        return Arrays.stream(c.getMethods())
                     .filter(m -> hasCasualFieldAnnotation(m))
                     .collect(Collectors.toList());
    }

    public static Map<Method, List<AnnotatedParameterInfo>> getCasuallyAnnotatedParameters(final Class<?> c)
    {
        List<Method> methods = Arrays.stream(c.getMethods())
                                     .collect(Collectors.toList());
        Map<Method, List<AnnotatedParameterInfo>> map = new HashMap<>();
        for(Method m : methods)
        {
            List<AnnotatedParameterInfo> l = getAnnotatedParameterInfo(m);
            if(!l.isEmpty())
            {
                map.put(m, l);
            }
        }
        return map;
    }

    public static List<AnnotatedParameterInfo> getAnnotatedParameterInfo(final Method m)
    {
        List<AnnotatedParameterInfo> parameterInfoList = new ArrayList<>();
        List<Class<?>> types = Arrays.stream(m.getParameterTypes()).collect(Collectors.toList());
        List<Type> genericParameterTypes = Arrays.stream(m.getGenericParameterTypes()).collect(Collectors.toList());
        List<List<Annotation>> l = Arrays.stream(m.getParameterAnnotations())
                                         .map(v -> Arrays.stream(v)
                                                          .filter(a -> a instanceof CasualFieldElement)
                                                          .collect(Collectors.toList()))
                                         .filter(f -> !f.isEmpty())
                                         .collect(Collectors.toList());
        if(!l.isEmpty())
        {
            if(genericParameterTypes.size() != l.size())
            {
                throw new FieldedUnmarshallingException("# of annotations: " + l.size() + " does not match # of types: " + genericParameterTypes.size());
            }
            for(int i = 0; i < genericParameterTypes.size(); ++i)
            {
                boolean isParameterizedType = genericParameterTypes.get(i) instanceof ParameterizedType;
                parameterInfoList.add(AnnotatedParameterInfo.of(l.get(i), types.get(i), isParameterizedType ? Optional.of((ParameterizedType)genericParameterTypes.get(i)) : Optional.empty()));
            }
        }
        return parameterInfoList;
    }

    public static boolean hasCasualFieldAnnotation(final Field f)
    {
        return null != f.getAnnotation(CasualFieldElement.class);
    }

    public static boolean hasCasualFieldAnnotation(final Method m)
    {
        return null != m.getAnnotation(CasualFieldElement.class);
    }

    public static boolean isArrayType(Class<?> o)
    {
        return o.isArray();
    }

    public static boolean isListType(Class<?> o)
    {
        return o.isAssignableFrom(ArrayList.class);
    }

    public static boolean isFieldedType(Class<?> c)
    {
        return FieldType.isOfFieldType(c);
    }

    public static Class<?> wrapIfPrimitive(Class<?> c)
    {
        return c.isPrimitive() ? mapper.get(c) : c;
    }

    public static Object adaptValueToFielded(Object v)
    {
        // integers are transported as long
        return v.getClass().equals(Integer.class) ? Long.valueOf((int)v) : v;
    }

    public static Class<?> adaptTypeToFielded(Class<?> clazz)
    {
        return clazz.equals(Integer.class) ? Long.class : clazz;
    }

    public static Optional<String> getListLengthName(final CasualFieldElement annotation)
    {
        return annotation.lengthName().isEmpty() ? Optional.empty() : Optional.of(annotation.lengthName());
    }

    // squid:S1452 - generic wildcard
    @SuppressWarnings({"unchecked", "squid:S1452"})
    public static Optional<Function<Object, ? extends Object>> getMapperTo(CasualFieldElement annotation)
    {
        final CasualObjectMapper<Object, ? extends Object> instance;
        try
        {
            instance = (CasualObjectMapper<Object, ? extends Object>)annotation.mapper().newInstance();
            return instance.getClass().equals(PassThroughMapper.class) ? Optional.empty() : Optional.of(v -> instance.to(v));
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new FieldedMarshallingException("can not get object mapper for: " + annotation, e);
        }
    }

    // squid:S1452 - generic wildcard
    @SuppressWarnings({"unchecked", "squid:S1452"})
    public static Optional<Pair<Function<Object, ? extends Object>, Class<?>>> getMapperFrom(CasualFieldElement annotation)
    {
        final CasualObjectMapper<? extends Object, Object> instance;
        try
        {
            instance = (CasualObjectMapper<? extends Object, Object>)annotation.mapper().newInstance();
            return instance.getClass().equals(PassThroughMapper.class) ? Optional.empty() : Optional.of(Pair.of(v -> instance.from(v), instance.getDstType()));
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new FieldedUnmarshallingException("can not get object mapper for: " + annotation, e);
        }
    }

}
