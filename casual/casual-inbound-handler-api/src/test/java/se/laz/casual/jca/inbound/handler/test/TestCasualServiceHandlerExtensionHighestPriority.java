/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.test;

import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.service.CasualServiceHandlerExtensionContext;
import se.laz.casual.jca.inbound.handler.service.CasualServiceHandlerExtension;
import se.laz.casual.jca.inbound.handler.service.DefaultCasualServiceHandlerExtensionContext;
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceEntry;
import se.laz.casual.spi.Priority;

import java.util.logging.Logger;

public class TestCasualServiceHandlerExtensionHighestPriority implements CasualServiceHandlerExtension
{
    @Override
    public CasualServiceHandlerExtensionContext before(InboundRequest request, BufferHandler bufferHandler)
    {
        return new DefaultCasualServiceHandlerExtensionContext();
    }

    @Override
    public Object[] convert(CasualServiceHandlerExtensionContext state, Object[] params)
    {
        return params;
    }

    @Override
    public void after(CasualServiceHandlerExtensionContext state)
    {}

    @Override
    public void handleError(CasualServiceHandlerExtensionContext state, InboundRequest request, InboundResponse.Builder responseBuilder, Throwable e, Logger logger)
    {}

    @Override
    public Priority getPriority()
    {
        return Priority.LEVEL_9;
    }
}
