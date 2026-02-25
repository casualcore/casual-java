/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network;

import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;

import java.util.Optional;

public final class ExceptionTool
{
    private ExceptionTool()
    {}
    public static Optional<CasualDecoderException> findDecoderException(Throwable t)
    {
        Throwable result = t;
        while(result != null)
        {
            if(result instanceof CasualDecoderException exception)
            {
                return Optional.of(exception);
            }
            Throwable cause = result.getCause();
            if(cause == result)
            {
                break;
            }
            result = cause;
        }
        return Optional.empty();
    }

    public static Optional<CasualProtocolException> findProtocolException(Throwable t)
    {
        Throwable result = t;
        while(result != null)
        {
            if(result instanceof CasualProtocolException exception)
            {
                return Optional.of(exception);
            }
            Throwable cause = result.getCause();
            if(cause == result)
            {
                break;
            }
            result = cause;
        }
        return Optional.empty();
    }

}
