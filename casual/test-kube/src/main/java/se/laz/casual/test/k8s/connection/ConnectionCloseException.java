/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.connection;

public class ConnectionCloseException extends ConnectionException
{
    private static final long serialVersionUID = 1L;

    public ConnectionCloseException()
    {
    }

    public ConnectionCloseException( String message )
    {
        super( message );
    }

    public ConnectionCloseException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ConnectionCloseException( Throwable cause )
    {
        super( cause );
    }
}
