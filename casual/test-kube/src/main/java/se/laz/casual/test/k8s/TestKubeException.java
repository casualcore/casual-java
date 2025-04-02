/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s;

public class TestKubeException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public TestKubeException()
    {
        super();
    }

    public TestKubeException( String message )
    {
        super( message );
    }

    public TestKubeException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public TestKubeException( Throwable cause )
    {
        super( cause );
    }
}
