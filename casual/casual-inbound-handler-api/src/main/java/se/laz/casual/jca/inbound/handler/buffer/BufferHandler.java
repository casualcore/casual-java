/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer;

import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.Prioritisable;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public interface BufferHandler extends Prioritisable
{
    boolean canHandleBuffer( String bufferType );

    ServiceCallInfo fromRequest(Proxy p, Method method, InboundRequest request );

    InboundResponse toResponse(Object result );
}
