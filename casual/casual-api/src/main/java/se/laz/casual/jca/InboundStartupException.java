/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import se.laz.casual.api.CasualRuntimeException;

public class InboundStartupException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;

    public InboundStartupException()
    {
    }

    public InboundStartupException( String message )
    {
        super( message );
    }

    public InboundStartupException( Throwable t )
    {
        super( t );
    }

    public InboundStartupException( String message, Throwable t )
    {
        super( message, t );
    }
}
