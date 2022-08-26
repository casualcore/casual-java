/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.entities;

import se.laz.casual.jca.CasualConnectionFactory;

import java.util.Objects;

public class ConnectionFactoryEntry
{
    private final ConnectionFactoryProducer connectionFactoryProducer;

    private ConnectionFactoryEntry(ConnectionFactoryProducer connectionFactoryProducer)
    {
        this.connectionFactoryProducer = connectionFactoryProducer;
    }

    public static ConnectionFactoryEntry of(ConnectionFactoryProducer connectionFactoryProducer)
    {
        Objects.requireNonNull(connectionFactoryProducer, "CasualConnectionFactoryProducer can not be null");
        return new ConnectionFactoryEntry(connectionFactoryProducer);
    }

    public String getJndiName()
    {
        return connectionFactoryProducer.getJndiName();
    }

    public CasualConnectionFactory getConnectionFactory()
    {
        return connectionFactoryProducer.getConnectionFactory();
    }

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
        ConnectionFactoryEntry that = (ConnectionFactoryEntry) o;
        return connectionFactoryProducer.equals(that.connectionFactoryProducer);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(connectionFactoryProducer);
    }

    @Override
    public String toString()
    {
        return "ConnectionFactoryEntry{" +
                "connectionFactoryProducer=" + connectionFactoryProducer +
                '}';
    }
}
