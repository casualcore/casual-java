package se.kodarkatten.casual.api.buffer.type.fielded.marshalling.details;

import se.kodarkatten.casual.api.buffer.type.fielded.FieldedData;
import se.kodarkatten.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public final class Unmarshaller
{
    private Unmarshaller()
    {}

    // for now fields only
    public static <T> T createObject(final FieldedTypeBuffer b, final Class<T> clazz)
    {
        Constructor<T> constructor = null;
        boolean constructorAccessible = false;
        try
        {
            constructor = clazz.getDeclaredConstructor();
            constructorAccessible = constructor.isAccessible();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            List<Field> fields = CommonDetails.getCasuallyAnnotatedFields(instance);
            handleFields(b, instance, fields);
            return instance;
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
        {
            throw new FieldedUnmarshallingException(e);
        }
        finally
        {
            if(null != constructor)
            {
                constructor.setAccessible(constructorAccessible);
            }
        }
    }

    private static <T> void handleFields(FieldedTypeBuffer b, T instance, List<Field> fields) throws IllegalAccessException
    {
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
                setFieldValue(instance, b, f, annotation);
            }
            finally
            {
                f.setAccessible(fieldAccessible);
            }
        }
    }


    public static <T> void setFieldValue(T instance, FieldedTypeBuffer b, Field f, CasualFieldElement annotation) throws IllegalAccessException
    {
        Class<?> wrappedType = CommonDetails.wrapIfPrimitive(f.getType());
        boolean castToInt = wrappedType.equals(Integer.class);
        if(CommonDetails.isListType(wrappedType))
        {
            setListFieldValue(instance, b, f, annotation);
        }
        else if(CommonDetails.isArrayType(wrappedType))
        {
            setArrayFieldValue(instance, b, f, annotation);
        }
        else if(CommonDetails.isFieldedType(wrappedType) || wrappedType.equals(Integer.class))
        {
            f.set(instance, toObject(b.read(annotation.name()), castToInt));
        }
        else
        {
            // should be POJO type
            f.set(instance, createObject(b, f.getType()));
        }
    }

    public static <T> void setArrayFieldValue(T instance, FieldedTypeBuffer b, Field f, CasualFieldElement annotation) throws IllegalAccessException
    {
        Class<?> componentType = CommonDetails.wrapIfPrimitive(f.getType().getComponentType());
        boolean castToInt = componentType.equals(Integer.class);
        List<FieldedData<?>> fieldedData = b.readAll(annotation.name());
        Object array = Array.newInstance(f.getType().getComponentType(), fieldedData.size());
        for(int i = 0; i < fieldedData.size(); ++i)
        {
            Array.set(array, i, toObject(fieldedData.get(i), castToInt));
        }
        f.set(instance, array);
    }

    public static <T> void setListFieldValue(T instance, FieldedTypeBuffer b, Field f, CasualFieldElement annotation) throws IllegalAccessException
    {
        boolean castToInt = false;
        Type listType = f.getGenericType();
        if(listType instanceof ParameterizedType)
        {
            Type elementType = ((ParameterizedType)listType).getActualTypeArguments()[0];
            castToInt = elementType.equals(Integer.class);
        }
        final boolean finalCastToInt = castToInt;
        List<Object> l = b.readAll(annotation.name()).stream()
                          .map(v -> toObject(v, finalCastToInt))
                          .collect(Collectors.toList());
        f.set(instance, l);
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
