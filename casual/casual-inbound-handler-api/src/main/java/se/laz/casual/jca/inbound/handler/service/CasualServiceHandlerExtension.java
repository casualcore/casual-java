/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service;

import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
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
    /**
     * Called before any service method invocation
     * An extension can create their own context to store whatever information they need in the subsequent steps
     * @param request - The inbound request
     * @param bufferHandler - The buffer handler chosen for the request
     * @return A context that will be passed to each subsequent invocation during one request
     */
    CasualServiceHandlerExtensionContext before(InboundRequest request, BufferHandler bufferHandler);

    /**
     * Allows the extension to handle service method params
     *
     * @param context - The extension context
     * @param params - Service method params
     * @return The params that will be used in the service invocation
     */
    Object[] convert(CasualServiceHandlerExtensionContext context, Object[] params);

    /**
     * Allows the extension to handle any context cleanup
     * Is always called regardless of method invocation outcome
     * @param context - The extension context
     */
    void after(CasualServiceHandlerExtensionContext context);

    /**
     * If method fails due to some exception this is called
     * @param context - The extension context
     * @param request
     * @param responseBuilder
     * @param e
     * @param logger
     */
    void handleError(CasualServiceHandlerExtensionContext context, InboundRequest request, InboundResponse.Builder responseBuilder, Throwable e, Logger logger);

    /**
     * The happy path, the response contains the result of the method invocation
     * @param response
     * @return An inbound response
     */
    default InboundResponse handleSuccess(InboundResponse response)
    {
        return response;
    }

    /**
     * Determines if this extension is appropriate for name
     * @param name
     * @return
     */
    default boolean canHandle(String name)
    {
        return name.equals(DefaultCasualServiceHandler.class.getName());
    }
}
