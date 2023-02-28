/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.test;

import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.service.CasualServiceHandlerExtensionState;
import se.laz.casual.jca.inbound.handler.service.CasualServiceHandlerExtension;
import se.laz.casual.jca.inbound.handler.service.DefaultCasualServiceHandlerExtensionState;
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceEntry;
import se.laz.casual.jca.inbound.handler.service.casual.DefaultCasualServiceHandler;
import se.laz.casual.spi.Priority;

import java.util.logging.Logger;

public class TestCasualServiceHandlerExtensionLowestPriority implements CasualServiceHandlerExtension
{
    @Override
    public boolean canHandle(String name)
    {
        return name.equals(DefaultCasualServiceHandler.class.getName());
    }

    @Override
    public CasualServiceHandlerExtensionState before(Object r, CasualServiceEntry entry, InboundRequest request, BufferHandler bufferHandler)
    {
        return new DefaultCasualServiceHandlerExtensionState();
    }

    @Override
    public Object[] convert(CasualServiceHandlerExtensionState state, Object[] params)
    {
        return params;
    }

    @Override
    public void after(CasualServiceHandlerExtensionState state)
    {}

    @Override
    public void handleError(CasualServiceHandlerExtensionState state, InboundRequest request, InboundResponse.Builder responseBuilder, Throwable e, Logger logger)
    {}

    @Override
    public Priority getPriority()
    {
        return Priority.LEVEL_0;
    }
}
