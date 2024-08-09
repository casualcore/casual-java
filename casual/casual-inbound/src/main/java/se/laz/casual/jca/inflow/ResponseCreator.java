/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import jakarta.resource.spi.work.WorkEvent;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.jca.inflow.work.CasualServiceCallWork;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResponseCreator
{
    private static final Logger LOG = Logger.getLogger(ResponseCreator.class.getName());
    private ResponseCreator()
    {}
    public static ServiceCallResult create(WorkEvent event, WorkResponseContext context, boolean isTpNoReply)
    {
        Objects.requireNonNull(event, "event can not be null");
        Objects.requireNonNull(context, "context can not be null");
        if(!isTpNoReply && event.getWork() instanceof CasualServiceCallWork casualServiceCallWork)
        {
            return null == casualServiceCallWork.getResponse() ? errorResponse(event.getException(), context) : ServiceCallResult.createBuilder()
                                                                                                                                 .withResult(casualServiceCallWork.getResponse())
                                                                                                                                 .withResultCode(casualServiceCallWork.getResponse().getMessage().getError())
                                                                                                                                 .build();
        }
        else if(isTpNoReply && null == event.getException())
        {
            return ServiceCallResult.createBuilder()
                                    .withResultCode(ErrorState.OK)
                                    .build();
        }
        if(null != event.getException())
        {
            log(event.getException(), context);
        }
        return ServiceCallResult.createBuilder()
                                .withResultCode(ErrorState.TPESYSTEM)
                                .build();
    }
    private static ServiceCallResult errorResponse(Exception exception, WorkResponseContext context)
    {
        log(exception, context);
        CasualServiceCallReplyMessage reply = CasualServiceCallReplyMessage.createBuilder()
                                                                           .setXid( context.xid() )
                                                                           .setExecution( context.execution() )
                                                                           .setError( ErrorState.TPESYSTEM )
                                                                           .setTransactionState( TransactionState.ROLLBACK_ONLY )
                                                                           .setServiceBuffer( ServiceBuffer.empty() )
                                                                           .build();
        return ServiceCallResult.createBuilder()
                                .withResult(CasualNWMessageImpl.of( context.correlationId(), reply))
                                .withResultCode(ErrorState.TPESYSTEM)
                                .build();
    }
    public static void log(Exception exception, WorkResponseContext context)
    {
        LOG.log(Level.WARNING, exception, () -> "inbound call for: " + context + " failed\n" + getStackStrace(exception) + "\ncause: " + getCause(exception));
    }
    private static String getCause(Exception exception)
    {
        return null == exception || null == exception.getCause()  ? "" : exception.getCause().toString();
    }
    private static String getStackStrace(Exception exception)
    {
        if(null == exception)
        {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
}
