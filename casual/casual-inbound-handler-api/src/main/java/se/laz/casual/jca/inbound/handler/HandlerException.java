/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler;

import se.laz.casual.api.CasualRuntimeException;

public class HandlerException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;

    public HandlerException(String message)
    {
        super( message );
    }

    public HandlerException(Throwable t )
    {
        super( t );
    }

    public HandlerException(String message, Throwable t )
    {
        super( message, t );
    }


}
