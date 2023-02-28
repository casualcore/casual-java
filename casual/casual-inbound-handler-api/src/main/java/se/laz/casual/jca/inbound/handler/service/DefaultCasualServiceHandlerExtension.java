/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service;

import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceEntry;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultCasualServiceHandlerExtension implements CasualServiceHandlerExtension
{
    @Override
    public Object[] convert(CasualServiceHandlerExtensionState state, Object[] params)
    {
        return params;
    }

    @Override
    public CasualServiceHandlerExtensionState before(Object r, CasualServiceEntry entry, InboundRequest request, BufferHandler bufferHandler)
    {
        return new DefaultCasualServiceHandlerExtensionState();
    }

    @Override
    public void after(CasualServiceHandlerExtensionState state)
    {}

    @Override
    public void handleError(CasualServiceHandlerExtensionState state, InboundRequest request, InboundResponse.Builder responseBuilder, Throwable e, Logger logger)
    {
        logger.log( Level.WARNING, e, ()-> "Error invoking fielded: " + e.getMessage() );
        responseBuilder
                .errorState( ErrorState.TPESVCERR)
                .transactionState( TransactionState.ROLLBACK_ONLY );
    }
}
