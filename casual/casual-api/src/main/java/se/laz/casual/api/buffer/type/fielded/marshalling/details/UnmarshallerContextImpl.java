/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessorMode;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class UnmarshallerContextImpl<T> implements UnmarshallerContext<T>
{
    private final FieldedTypeBuffer buffer;
    private int index;
    private final FieldedTypeBufferProcessorMode mode;
    private List<ParameterInfo> parameterInfo;
    private final Method method;
    private final Class<T> clazz;
    private T instance;

    private UnmarshallerContextImpl(FieldedTypeBuffer buffer, int index, FieldedTypeBufferProcessorMode mode, Class<T> clazz)
    {
        this(buffer, index, mode, clazz, null);
    }

    private UnmarshallerContextImpl(FieldedTypeBuffer buffer, int index, FieldedTypeBufferProcessorMode mode, Method method)
    {
        this(buffer, index, mode, null, method);
    }

    private UnmarshallerContextImpl(FieldedTypeBuffer buffer, int index, FieldedTypeBufferProcessorMode mode, Class<T> clazz, Method method)
    {
        this.buffer = buffer;
        this.index = index;
        this.mode = mode;
        this.method = method;
        this.clazz = clazz;
    }

    public static <Y> UnmarshallerContext<Y> of(FieldedTypeBuffer buffer, int index, FieldedTypeBufferProcessorMode mode, Method method)
    {
        Objects.requireNonNull(buffer, "buffer can not be null");
        Objects.requireNonNull(mode, "mode can not be null");
        Objects.requireNonNull(method, "method can not be null");
        if(index < 0)
        {
            throw new IndexOutOfBoundsException("index can not be less than 0, is: " + index);
        }
        return new UnmarshallerContextImpl<>(buffer, index, mode, method);
    }

    public static <Y> UnmarshallerContext<Y> of(FieldedTypeBuffer buffer, int index, FieldedTypeBufferProcessorMode mode, Class<Y> clazz)
    {
        Objects.requireNonNull(buffer, "buffer can not be null");
        Objects.requireNonNull(mode, "mode can not be null");
        Objects.requireNonNull(clazz, "class can not be null");
        if(index < 0)
        {
            throw new IndexOutOfBoundsException("index can not be less than 0, is: " + index);
        }
        return new UnmarshallerContextImpl<>(buffer, index, mode, clazz);
    }

    public static <X,Y> UnmarshallerContext<Y> of(UnmarshallerContext<X> context, Class<Y> clazz, int index)
    {
        return of(context.getBuffer(), index, context.getMode(), clazz);
    }

    public static <X,Y> UnmarshallerContext<Y> of(UnmarshallerContext<X> context, Method method, int index)
    {
        return of(context.getBuffer(), index, context.getMode(), method);
    }

    @Override
    public T getInstance()
    {
        if(null == clazz)
        {
            throw new FieldedUnmarshallingException("class is null");
        }
        if(instance == null)
        {
            instance = createInstance(clazz);
        }
        return instance;
    }

    @Override
    public List<ParameterInfo> getParameterInfo()
    {
        if(null == method)
        {
            throw new FieldedUnmarshallingException("method is null");
        }
        if(null == parameterInfo)
        {
            parameterInfo = CommonDetails.getParameterInfo(method);
        }
        return parameterInfo;
    }

    @Override
    public Map<Method, List<ParameterInfo>> getMethodInfo()
    {
        return CommonDetails.getParameterInfo(getInstance().getClass());
    }

    @Override
    public FieldedTypeBuffer getBuffer()
    {
        return buffer;
    }

    @Override
    public int getIndex()
    {
        return index;
    }

    @Override
    public void increaseIndex()
    {
        ++index;
    }

    @Override
    public FieldedTypeBufferProcessorMode getMode()
    {
        return mode;
    }

    @Override
    public List<Field> getFields()
    {
        return CommonDetails.getCasuallyAnnotatedFields(getInstance().getClass());
    }

    private T createInstance(Class<T> clazz)
    {
        boolean accessible = false;
        Constructor<T> constructor = null;
        try
        {
            constructor = clazz.getDeclaredConstructor();
            accessible = constructor.isAccessible();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            throw new FieldedUnmarshallingException("Missing NOP constructor for class: " + clazz, e);
        }
        finally
        {
            if(null != constructor)
            {
                constructor.setAccessible(accessible);
            }
        }
    }

    @Override
    public String toString()
    {
        return "UnmarshallerContextImpl{" +
                "buffer=" + buffer +
                ", index=" + index +
                ", mode=" + mode +
                ", parameterInfo=" + parameterInfo +
                ", method=" + method +
                ", clazz=" + clazz +
                ", instance=" + instance +
                '}';
    }
}
