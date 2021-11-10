/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import se.laz.casual.network.CasualDecoderException;
import se.laz.casual.network.connection.CasualConnectionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ExceptionHandler extends ChannelInboundHandlerAdapter
{
    private final Correlator correlator;
    private final OnNetworkError onNetworkError;

    private ExceptionHandler(Correlator correlator, OnNetworkError onNetworkError)
    {
        this.correlator = correlator;
        this.onNetworkError = onNetworkError;
    }

    public static ExceptionHandler of(final Correlator correlator, OnNetworkError onNetworkError)
    {
        Objects.requireNonNull(correlator, "correlator can not be null");
        Objects.requireNonNull(onNetworkError, "onNetworkError can not be null");
        return new ExceptionHandler(correlator, onNetworkError);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        onNetworkError.notifyListenerIfNotConnected(ctx.channel());
        completeReqExceptionally(cause);
    }

    private void completeReqExceptionally( Throwable cause)
    {
        Optional<CasualDecoderException> d = findDecoderException(cause);
        if(d.isPresent())
        {
            CasualDecoderException e = d.get();
            completeReqExceptionally(e, e.getCorrid());
            return;
        }
        // something has gone horribly wrong, complete all requests exceptionally
        correlator.completeAllExceptionally(new CasualConnectionException(cause));
    }

    private void completeReqExceptionally(CasualDecoderException e, UUID corrid)
    {
        List<UUID> l = new ArrayList<>();
        l.add(corrid);
        correlator.completeExceptionally(l, e);
    }

    private Optional<CasualDecoderException> findDecoderException(Throwable t)
    {
        Throwable cause;
        Throwable result = t;
        while(null != (cause = result.getCause()) && (result != cause))
        {
            if(result instanceof  CasualDecoderException)
            {
                return Optional.of((CasualDecoderException)result);
            }
            result = cause;
        }
        return Optional.empty();
    }

}
