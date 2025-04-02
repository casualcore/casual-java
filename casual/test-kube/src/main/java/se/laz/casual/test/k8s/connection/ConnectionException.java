/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.connection;

import se.laz.casual.test.k8s.TestKubeException;

public class ConnectionException extends TestKubeException
{
    private static final long serialVersionUID = 1L;

    public ConnectionException()
    {
        super();
    }

    public ConnectionException( String message )
    {
        super( message );
    }

    public ConnectionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ConnectionException( Throwable cause )
    {
        super( cause );
    }
}
