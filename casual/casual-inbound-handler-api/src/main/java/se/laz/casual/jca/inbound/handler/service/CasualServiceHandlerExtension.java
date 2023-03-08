/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service;

import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceEntry;
import se.laz.casual.jca.inbound.handler.service.casual.DefaultCasualServiceHandler;
import se.laz.casual.spi.GenericExtensionPoint;
import se.laz.casual.spi.Prioritisable;

import java.util.logging.Logger;

/**
 * The order of the calls is as follows:
 * before
 * convert
 * actual service call
 * after
 * handleError - only if service call triggers some exception
 *
 * Note that before has to return something derived from {@link CasualServiceHandlerExtensionContext}, this is where you would store any eventual - per call, state.
 * If you do not have a need to do that you can just return the one and the same instance of {@link DefaultCasualServiceHandlerExtensionContext}.
 */
public interface CasualServiceHandlerExtension extends Prioritisable, GenericExtensionPoint
{
    CasualServiceHandlerExtensionContext before(Object r, CasualServiceEntry entry, InboundRequest request, BufferHandler bufferHandler);
    Object[] convert(CasualServiceHandlerExtensionContext context, Object[] params);
    void after(CasualServiceHandlerExtensionContext context);
    void handleError(CasualServiceHandlerExtensionContext context, InboundRequest request, InboundResponse.Builder responseBuilder, Throwable e, Logger logger);
    default boolean canHandle(String name)
    {
        return name.equals(DefaultCasualServiceHandler.class.getName());
    }
}
