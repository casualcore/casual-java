/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class InstanceCreator
{
    private InstanceCreator()
    {}
    @SuppressWarnings("java:S3011")
    public static <T> T createInstance(Class<T> clazz)
    {
        try
        {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            throw new FieldedUnmarshallingException("Missing NOP constructor for class: " + clazz, e);
        }
    }
}
