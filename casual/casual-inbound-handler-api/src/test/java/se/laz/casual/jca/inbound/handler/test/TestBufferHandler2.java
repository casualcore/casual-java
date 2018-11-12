/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.test;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.jca.inbound.handler.Priority;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.buffer.ServiceCallInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TestBufferHandler2 implements BufferHandler
{
    public static final String BUFFER_TYPE_2 = "buffertype1";
    public static final String BUFFER_COMMON = "commonBuffer";

    @Override
    public boolean canHandleBuffer(String bufferType)
    {
        return BUFFER_TYPE_2.equals( bufferType ) || BUFFER_COMMON.equals( bufferType );
    }

    @Override
    public ServiceCallInfo fromBuffer(Proxy p, Method method, CasualBuffer buffer)
    {
        return null;
    }

    @Override
    public CasualBuffer toBuffer(Object result)
    {
        return null;
    }

    @Override
    public Priority getPriority()
    {
        return Priority.LEVEL_2;
    }
}
