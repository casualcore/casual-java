/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer;

import se.laz.casual.api.buffer.CasualBuffer;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Pass Through Buffer Handler to facilitate dispatch
 * of a service call with an unknown buffer type.
 */
public class PassThroughBufferHandler implements BufferHandler
{
    @Override
    public boolean canHandleBuffer(String bufferType)
    {
        return true;
    }

    @Override
    public ServiceCallInfo fromBuffer(Proxy p, Method method, CasualBuffer buffer)
    {
        return ServiceCallInfo.of( method, new Object[]{buffer} );
    }

    @Override
    public CasualBuffer toBuffer(Object result)
    {
        return (CasualBuffer)result;
    }
}
