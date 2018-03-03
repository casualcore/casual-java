/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer;

import se.laz.casual.api.buffer.CasualBuffer;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public interface BufferHandler
{
    boolean canHandleBuffer( String bufferType );

    ServiceCallInfo fromBuffer(Proxy p, Method method, CasualBuffer buffer );

    CasualBuffer toBuffer( Object result );
}
