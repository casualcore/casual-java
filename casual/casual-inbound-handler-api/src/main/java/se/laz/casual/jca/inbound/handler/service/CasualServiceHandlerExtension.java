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

public interface CasualServiceHandlerExtension extends Prioritisable, GenericExtensionPoint
{
    void before(Object r, CasualServiceEntry entry, InboundRequest request, BufferHandler bufferHandler);
    Object[] convert(Object[] params);
    void after();
    void handleError(InboundRequest request, InboundResponse.Builder responseBuilder, Throwable e, Logger logger);
    default boolean canHandle(String name)
    {
        return name.equals(DefaultCasualServiceHandler.class.getName());
    }
}
