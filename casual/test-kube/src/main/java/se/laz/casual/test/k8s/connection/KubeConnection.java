/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.connection;

public interface KubeConnection extends AutoCloseable
{
    /**
     * Type of connection.
     *
     * @return the type of the connection.
     */
    KubeConnectionType getType();

    /**
     * Host name for the connection.
     *
     * @return connections host name.
     */
    String getHostName();

    /**
     * Port for the connection.
     *
     * @return connections port.
     */
    int getPort();

    @Override
    void close();
}
