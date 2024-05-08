/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.network;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Helper factory to find available network ports.
 * Primarily for test purposes, to allow multiple concurrent unit tests.
 */
public final class NetworkPortFactory
{
    private NetworkPortFactory()
    {
    }

    /**
     * Get an available port.
     *
     * @return currently available port.
     */
    public static int getAvailablePort()
    {
        return createTestSocket( 0 );
    }

    /**
     * Check if a specific port is available.
     *
     * @param port to check.
     * @return if the port is currently available.
     */
    public static boolean isPortAvailable( int port )
    {
        try
        {
            createTestSocket( port );
            return true;
        }
        catch( IllegalStateException e )
        {
            return false;
        }
    }

    private static int createTestSocket( int port )
    {
        try( ServerSocket socket = new ServerSocket( port ) )
        {
            return socket.getLocalPort();
        }
        catch( IOException e )
        {
            throw new IllegalStateException( "Unexpected exception whilst trying to find an available port." );
        }
    }
}
