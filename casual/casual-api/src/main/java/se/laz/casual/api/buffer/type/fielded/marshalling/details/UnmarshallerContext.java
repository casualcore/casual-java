/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessorMode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public interface UnmarshallerContext<T>
{
    List<ParameterInfo> getParameterInfo();
    Map<Method, List<ParameterInfo>> getMethodInfo();
    FieldedTypeBuffer getBuffer();
    int getIndex();
    void increaseIndex();
    FieldedTypeBufferProcessorMode getMode();
    T getInstance();
    List<Field> getFields();
}
