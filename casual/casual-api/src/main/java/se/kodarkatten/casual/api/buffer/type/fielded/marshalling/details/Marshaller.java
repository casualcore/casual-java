package se.kodarkatten.casual.api.buffer.type.fielded.marshalling.details;

import se.kodarkatten.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.FieldedMarshallingException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public final class Marshaller
{
    private Marshaller()
    {
    }

    public static FieldedTypeBuffer writeFields(final Object o, FieldedTypeBuffer b)
    {
        List<Field> fields = CommonDetails.getCasuallyAnnotatedFields(o);
        writeFields(o, fields, b);
        List<Method> methods = CommonDetails.getCasuallyAnnotatedMethods(o);
        return writeMethodReturnValues(o, methods, b);
    }

    private static FieldedTypeBuffer writeMethodReturnValues(Object o, List<Method> methods, FieldedTypeBuffer b)
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
                Object v = CommonDetails.wrapIfPrimitive(returnValue.getClass()).cast(returnValue);
                v = CommonDetails.adaptValueToFielded(v);
                writeFielded(b, annotation, v);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                throw new FieldedMarshallingException("problems invoking method: " + m, e);
            }
            finally
            {
                m.setAccessible(access);
            }
        }
        return b;
    }

    public static FieldedTypeBuffer writeFields(final Object o, final List<Field> fields, FieldedTypeBuffer b)
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
                Object v = CommonDetails.wrapIfPrimitive(fieldValue.getClass()).cast(fieldValue);
                v = CommonDetails.adaptValueToFielded(v);
                writeFielded(b, annotation, v);
            }
            catch (IllegalAccessException e)
            {
                throw new FieldedMarshallingException("can't access field: " + f);
            }
            finally
            {
                f.setAccessible(access);
            }
        }
        return b;
    }

    public static void writeFielded(final FieldedTypeBuffer b, final CasualFieldElement annotation, final Object v) throws IllegalAccessException
    {
        if(CommonDetails.isListType(v.getClass()))
        {
            List<?> l = (List<?>)v;
            writeListType(b, annotation, l);
        }
        else if(CommonDetails.isArrayType(v.getClass()))
        {
            writeArrayType(b, annotation, v);
        }
        else if(CommonDetails.isFieldedType(v.getClass()))
        {
            b.write(annotation.name(), v);
        }
        else
        {
            // should be POJO type
            writeFields(v, b);
        }
    }

    public static void writeArrayType(final FieldedTypeBuffer b, final CasualFieldElement annotation, final Object o)
    {
        for(Object v : toObjectArray(o))
        {
            b.write(annotation.name(), CommonDetails.adaptValueToFielded(v));
        }
    }

    public static Object[] toObjectArray(Object array)
    {
        Class<?> componentType = array.getClass().getComponentType();
        if (componentType.isPrimitive())
        {
            int length = Array.getLength(array);
            Object[] r = new Object[length];
            for (int i = 0; i < length; ++i)
            {
                r[i] = Array.get(array, i);
            }
            return r;
        }
        return (Object[]) array;
    }

    public static void writeListType(final FieldedTypeBuffer b, final CasualFieldElement annotation, final List<?> l)
    {
        for(Object v : l)
        {
            b.write(annotation.name(), CommonDetails.adaptValueToFielded(v));
        }
    }

}
