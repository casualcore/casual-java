/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api;

/**
 * The base exception for all known exceptions thrown by casual-java
 */
public class CasualRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public CasualRuntimeException( )
    {
        super();
    }

    public CasualRuntimeException( String message )
    {
        super(  message );
    }

    public CasualRuntimeException( Throwable t )
    {
        super( t );
    }

    public CasualRuntimeException( String message, Throwable t )
    {
        super( message, t );
    }



}
