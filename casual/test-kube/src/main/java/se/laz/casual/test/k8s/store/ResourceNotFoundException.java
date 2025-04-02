/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.store;

import se.laz.casual.test.k8s.TestKubeException;

public class ResourceNotFoundException extends TestKubeException
{
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException()
    {
        super();
    }

    public ResourceNotFoundException( String message )
    {
        super( message );
    }

    public ResourceNotFoundException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ResourceNotFoundException( Throwable cause )
    {
        super( cause );
    }
}
