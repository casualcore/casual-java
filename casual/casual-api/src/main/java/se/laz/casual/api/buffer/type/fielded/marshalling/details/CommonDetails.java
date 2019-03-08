/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import se.laz.casual.api.buffer.type.fielded.FieldType;
import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.laz.casual.api.buffer.type.fielded.mapper.CasualObjectMapper;
import se.laz.casual.api.buffer.type.fielded.mapper.PassThroughMapper;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedMarshallingException;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;
import se.laz.casual.api.util.FluentMap;
import se.laz.casual.api.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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

/**
 * A collection of helper methods for marshalling/unmarshalling
 */
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

    /**
     * Gets the fields annotated with {@link CasualFieldElement}
     * @see CasualFieldElement
     * @param c the class
     * @return a list of fields that have the annotation
     */
    public static List<Field> getCasuallyAnnotatedFields(final Class<?> c)
    {
        return Arrays.stream(c.getDeclaredFields())
                     .filter(f -> hasCasualFieldAnnotation(f))
                     .collect(Collectors.toList());
    }

    /**
     * Gets the methods annotated with {@link CasualFieldElement}
     * @see CasualFieldElement
     * @param c the class
     * @return a list of methods that have the annotation
     */
    public static List<Method> getCasuallyAnnotatedMethods(final Class<?> c)
    {
        return Arrays.stream(c.getMethods())
                     .filter(m -> hasCasualFieldAnnotation(m))
                     .collect(Collectors.toList());
    }

    /**
     * Gets the parameter info for each annotated, {@link CasualFieldElement},  method of the class
     * @see ParameterInfo
     * @param c the class
     * @return a map with {method, parameter info}
     */
    public static Map<Method, List<ParameterInfo>> getParameterInfo(final Class<?> c)
    {
        List<Method> methods = Arrays.stream(c.getMethods())
                                     .collect(Collectors.toList());
        Map<Method, List<ParameterInfo>> map = new HashMap<>();
        for(Method m : methods)
        {
            List<ParameterInfo> l = getParameterInfo(m);
            if(!l.isEmpty())
            {
                map.put(m, l);
            }
        }
        return map;
    }

    /**
     * Gets the parameter info for the method - if annotated with {@link CasualFieldElement}
     * @see ParameterInfo
     * @param m the method
     * @return a list with ParameterInfo
     */
    public static List<ParameterInfo> getParameterInfo(final Method m)
    {
        List<Type> genericParameterTypes = Arrays.stream(m.getGenericParameterTypes()).collect(Collectors.toList());
        List<ParameterInfo> parameterInfo = new ArrayList<>();
        Parameter[] parameters = m.getParameters();
        for(int i = 0; i < m.getParameterCount(); ++i)
        {
            Parameter p = parameters[i];
            Optional<Annotation> a = Arrays.stream(p.getDeclaredAnnotations())
                                           .filter(v -> v instanceof CasualFieldElement)
                                           .findFirst();
            if(a.isPresent())
            {
                boolean isParameterizedType = genericParameterTypes.get(i) instanceof ParameterizedType;
                parameterInfo.add(AnnotatedParameterInfo.of(a.get(), p.getType(), isParameterizedType ? Optional.of((ParameterizedType)genericParameterTypes.get(i)) : Optional.empty()));
            }
            else
            {
                // if there's no annotation and the parameter type itself does not contain any @CasualFieldElement
                // then we just bail and continue
                if(!hasCasualFieldAnnotation(p.getType()))
                {
                    return new ArrayList<>();
                }
                parameterInfo.add(ParameterInfo.of(p.getType()));
            }
        }
        return parameterInfo;
    }

    /**
     * Predicate if the field is annotated with {@code CasualFieldElement}
     * @see CasualFieldElement
     * @param f the field
     * @return {@code true} if annotated
     *         {@code false} if not
     */
    public static boolean hasCasualFieldAnnotation(final Field f)
    {
        return null != f.getAnnotation(CasualFieldElement.class);
    }

    /**
     * Predicate if the method is annotated with {@code CasualFieldElement}
     * @see CasualFieldElement
     * @param m the method
     * @return {@code true} if annotated
     *         {@code false} false if not
     */
    public static boolean hasCasualFieldAnnotation(final Method m)
    {
        return null != m.getAnnotation(CasualFieldElement.class);
    }

    /**
     * Predicate if the class is of type array
     * @param c the class
     * @return {@code true} if it is
     *         {@code false} if not
     */
    public static boolean isArrayType(Class<?> c)
    {
        return c.isArray();
    }

    /**
     * Predicate if the class is of type ArrayList
     * @param c the class
     * @return {@code true} if it is
     *         {@code false} if not
     */
    public static boolean isListType(Class<?> c)
    {
        return c.isAssignableFrom(ArrayList.class);
    }

    /**
     * Predicate if the class is of type fielded
     * @see FieldType
     * @param c the class
     * @return {@code true} if it is
     *         {@code false} if not
     */
    public static boolean isFieldedType(Class<?> c)
    {
        return FieldType.isOfFieldType(c);
    }

    /**
     * Wraps the class if it is primitive
     * @param c the class
     * @return the class, wrapped if it is primitive
     */
    public static Class<?> wrapIfPrimitive(Class<?> c)
    {
        return c.isPrimitive() ? mapper.get(c) : c;
    }

    /**
     * Integers are handled as long so if the Object is of type Integer it will be adapted to Long
     * @param v the object
     * @return if of type integer an object of type Long, if not - the object itself
     */
    public static Object adaptValueToFielded(Object v)
    {
        // integers are transported as long
        return v.getClass().equals(Integer.class) ? Long.valueOf((int)v) : v;
    }

    /**
     * Integers are handled as long so if the Class is of type Integer it will be adapted to Long.class
     * @param clazz the class
     * @return if of type integer a class of type Long, if not - the class itself
     */
    public static Class<?> adaptTypeToFielded(Class<?> clazz)
    {
        return clazz.equals(Integer.class) ? Long.class : clazz;
    }

    /**
     * Finds the length name of the list - if any
     * @param annotation the annotation
     * @return The name or Optional.empty() if missing
     */
    public static Optional<String> getListLengthName(final CasualFieldElement annotation)
    {
        return annotation.lengthName().isEmpty() ? Optional.empty() : Optional.of(annotation.lengthName());
    }

    /**
     * Finds the object mapper to use - if any
     * @param annotation the annotation
     * @return A function mapping from one object to another or Optional.empty() if no mapper is found
     */
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

    /**
     * Finds the object mapper to use - if any
     * @param annotation the annotation
     * @return A pair with a function mapping from one object to another and the type or Optional.empty() if no mapper is found
     */
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

    private static boolean hasCasualFieldAnnotation(final Class<?> type)
    {
        for(Field f : type.getDeclaredFields())
        {
            if(hasCasualFieldAnnotation(f))
            {
                return true;
            }
        }
        for(Method m : type.getDeclaredMethods())
        {
            if(hasCasualFieldAnnotation(m))
            {
                return true;
            }
        }
        return false;
    }

}
