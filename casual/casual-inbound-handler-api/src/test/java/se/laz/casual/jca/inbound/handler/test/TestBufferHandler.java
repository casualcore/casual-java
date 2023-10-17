/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.test;

import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.buffer.InboundRequestInfo;
import se.laz.casual.spi.Priority;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.buffer.ServiceCallInfo;

public class TestBufferHandler implements BufferHandler
{
    public static final String BUFFER_TYPE_1 = "test123";
    public static final String BUFFER_COMMON  = "commonBuffer";

    @Override
    public boolean canHandleBuffer(String bufferType)
    {
        return BUFFER_TYPE_1.equals( bufferType ) || BUFFER_COMMON.equals( bufferType );
    }

    @Override
    public ServiceCallInfo fromRequest(InboundRequestInfo requestInfo, InboundRequest request)
    {
        return null;
    }

    @Override
    public InboundResponse toResponse(ServiceCallInfo info, Object result)
    {
        return null;
    }

    @Override
    public Priority getPriority()
    {
        return Priority.LEVEL_3;
    }
}
