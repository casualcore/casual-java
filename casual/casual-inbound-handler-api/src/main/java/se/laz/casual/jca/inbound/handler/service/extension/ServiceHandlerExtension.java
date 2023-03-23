/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service.extension;

import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;
import se.laz.casual.spi.Prioritisable;

/**
 * Provide integration points for Service Handler service invocations.
 *
 * The order of the calls is as follows:
 * <ol>
 *     <li>before</li>
 *     <li>convertRequestParams</li>
 *     <li>actual service call</li>
 *     <li>handleError / handleSuccess</li>
 *     <li>after</li>
 * </ol>
 *
 * handleError - is only called if service call triggers some exception.
 * handleSuccess - is only called if service call does not trigger some exception.
 * <br>
 * Note that before has to return something derived from {@link ServiceHandlerExtensionContext}, this is where
 * you would store any eventual - per call, state.
 * <br>
 * If you do not have a need to do that you can just leave the default implementation which returns
 * a static instance of {@link DefaultServiceHandlerExtensionContext}.
 *
 * Extension method implementations should never allow exceptions be thrown.
 *
 */
public interface ServiceHandlerExtension extends Prioritisable
{
    ServiceHandlerExtensionContext DEFAULT_CONTEXT = new DefaultServiceHandlerExtensionContext();
    /**
     * Called before any service method invocation.
     * An extension can create their own context to store whatever information they need in the subsequent steps.
     *
     * @param request - The inbound request
     * @param bufferHandler - The buffer handler chosen for the request
     * @return A context that will be passed to each subsequent invocation during one request
     */
    default ServiceHandlerExtensionContext before( InboundRequest request, BufferHandler bufferHandler)
    {
        return DEFAULT_CONTEXT;
    }

    /**
     * Allows the extension to modify method params.
     *
     * NB - the methods params must still match the target method otherwise it will likely break the call
     * to the service method as the methods parameters no longer match.
     *
     * @param context - the context for the service call.
     * @param params - Service method params
     * @return The params that will be used in the service invocation
     */
    default Object[] convertRequestParams( ServiceHandlerExtensionContext context, Object[] params)
    {
        return params;
    }

    /**
     * Allows the extension to handle any context cleanup
     * Is always called regardless of method invocation outcome
     * @param context - The extension context
     */
    default void after( ServiceHandlerExtensionContext context)
    {
        //Do nothing.
    }

    /**
     * If service invocation throws an exception, this method is called.
     *
     * By implementing this method you can for example modify the response that has been generated
     * by the service handler.
     *
     * @param context context for this service call.
     * @param request service request.
     * @param response current service handler response.
     * @param e catch exception that occured whilst calling the service.
     * @return the final response to be returned to the caller.
     */
    default InboundResponse handleError( ServiceHandlerExtensionContext context, InboundRequest request, InboundResponse response, Throwable e )
    {
        return response;
    }

    /**
     * If service invocation is successful, i.e. does not throw an exception, this method is called.
     *
     * By implementing this method you can for example modify the response that has been generated
     * by the {@link ServiceHandler}.
     *
     * @param context for this service call.
     * @param response service response
     * @return the final response to be returned to the caller.
     */
    default InboundResponse handleSuccess(ServiceHandlerExtensionContext context, InboundResponse response)
    {
        return response;
    }

    /**
     * Determines if this extension is appropriate for name provided.
     * Name used is determined by the selected ServiceHandler, though should match an annotation class.
     *
     * @param name provided by service handler to allow filtering of extensions usage.
     * @return if this extension can be used or not.
     */
    boolean canHandle( String name);
}
