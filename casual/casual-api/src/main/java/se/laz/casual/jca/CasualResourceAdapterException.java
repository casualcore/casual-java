/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import se.laz.casual.api.CasualRuntimeException;

public class CasualResourceAdapterException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;

    public CasualResourceAdapterException( )
    {
        super();
    }

    public CasualResourceAdapterException( String message )
    {
        super(  message );
    }

    public CasualResourceAdapterException( Throwable t )
    {
        super( t );
    }

    public CasualResourceAdapterException( String message, Throwable t )
    {
        super( message, t );
    }

}
