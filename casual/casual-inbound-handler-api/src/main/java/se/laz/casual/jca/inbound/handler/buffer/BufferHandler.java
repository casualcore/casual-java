/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer;

import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.spi.Prioritisable;

/**
 * Different buffers can require transformation before they are dispatched to their respective service.
 * This interface provides a mechanism for creating custom handlers.
 */
public interface BufferHandler extends Prioritisable
{
    /**
     * Determine whether this BufferHandler is able to deal buffers of the given type.
     *
     * @param bufferType of the buffer that needs to be transformed.
     * @return if the buffer type can be transformer or not.
     */
    boolean canHandleBuffer( String bufferType );

    /**
     * Convert an inbound request to the appropriate {@link ServiceCallInfo}.
     *
     * @param requestInfo further request information
     * @param request     that contains the buffer.
     * @return the transformed buffer and service call information.
     */
    ServiceCallInfo fromRequest(InboundRequestInfo requestInfo, InboundRequest request );

    /**
     * Convert the response of the service request back to the appropriate {@link InboundResponse}.
     *
     * @param info service information
     * @param result of calling the service - the result may be null in case of void service return
     * @return response containing result buffer and/or error data.
     */
    InboundResponse toResponse( ServiceCallInfo info, Object result );
}
