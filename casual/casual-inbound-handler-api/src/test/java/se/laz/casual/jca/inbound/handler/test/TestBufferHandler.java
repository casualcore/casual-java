/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.test;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.buffer.ServiceCallInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TestBufferHandler implements BufferHandler
{
    public static final String BUFFER_TYPE_1 = "test123";

    @Override
    public boolean canHandleBuffer(String bufferType)
    {
        return BUFFER_TYPE_1.equals( bufferType );
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
}
