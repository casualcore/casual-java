package se.kodarkatten.casual.api.buffer.type.fielded.marshalling.details;

import se.kodarkatten.casual.api.buffer.type.fielded.FieldType;
import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.kodarkatten.casual.api.util.FluentMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static List<Field> getCasuallyAnnotatedFields(final Object o)
    {
        return Arrays.stream(o.getClass().getDeclaredFields())
                     .filter(f -> hasCasualFieldAnnotation(f))
                     .collect(Collectors.toList());
    }


    public static List<Method> getCasuallyAnnotatedMethods(final Object o)
    {
        return Arrays.stream(o.getClass().getMethods())
                     .filter(m -> hasCasualFieldAnnotation(m))
                     .collect(Collectors.toList());
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
}
