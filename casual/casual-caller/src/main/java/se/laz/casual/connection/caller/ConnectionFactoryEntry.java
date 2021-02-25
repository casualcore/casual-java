/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

import se.laz.casual.jca.CasualConnectionFactory;

import java.util.Objects;

public final class ConnectionFactoryEntry
{
    private final String jndiName;
    private final CasualConnectionFactory connectionFactory;

    private ConnectionFactoryEntry(String jndiName, CasualConnectionFactory connectionFactory)
    {
        this.jndiName = jndiName;
        this.connectionFactory = connectionFactory;
    }

    public static ConnectionFactoryEntry of(String jndiName, CasualConnectionFactory connectionFactory)
    {
        Objects.requireNonNull(jndiName, "jndiName can not be null");
        Objects.requireNonNull(connectionFactory, "connectionFactory can not be null");
        return new ConnectionFactoryEntry(jndiName, connectionFactory);
    }

    public String getJndiName()
    {
        return jndiName;
    }

    public CasualConnectionFactory getConnectionFactory()
    {
        return connectionFactory;
    }


    // Note:
    // equals and hashCode on jndiname is enough since that is unique per connection factory
    // so no need to also match on the connection factory
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ConnectionFactoryEntry entry = (ConnectionFactoryEntry) o;
        return Objects.equals(jndiName, entry.jndiName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(jndiName);
    }
}
