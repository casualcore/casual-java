/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

public class ConnectionFactoryEntryChangedEvent
{
    private final String jndi;
    private final ConnectionFactoryEntry connectionFactoryEntry;

    public ConnectionFactoryEntryChangedEvent(String jndi, ConnectionFactoryEntry connectionFactoryEntry)
    {
        this.jndi = jndi;
        this.connectionFactoryEntry = connectionFactoryEntry;
    }

    public String getJndi()
    {
        return jndi;
    }

    public ConnectionFactoryEntry getConnectionFactoryEntry()
    {
        return connectionFactoryEntry;
    }
}
