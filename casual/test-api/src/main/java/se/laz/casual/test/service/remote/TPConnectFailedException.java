/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.test.service.remote;

public class TPConnectFailedException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public TPConnectFailedException()
    {
        super();
    }

    public TPConnectFailedException(String message)
    {
        super(message);
    }

    public TPConnectFailedException(Throwable t)
    {
        super(t);
    }

    public TPConnectFailedException(String message, Throwable t)
    {
        super(message, t);
    }
}
