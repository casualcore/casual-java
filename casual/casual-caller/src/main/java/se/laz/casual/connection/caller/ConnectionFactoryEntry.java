/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.CasualConnectionFactory;

import javax.resource.ResourceException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConnectionFactoryEntry
{
    private static final Logger LOG = Logger.getLogger(ConnectionFactoryEntry.class.getName());

    private final String jndiName;
    private final CasualConnectionFactory connectionFactory;

    /**
     * Connection factory entries should invalidate on connection errors and revalidate as soon as a new valid
     * connection can be established.
     */
    private boolean valid = true;

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

    public boolean isValid()
    {
        return valid;
    }

    public boolean isInvalid()
    {
        return !valid;
    }

    public void invalidate()
    {
        valid = false;
        LOG.finest(() -> "Invalidated CasualConnection with jndiName=" + jndiName);
    }

    public void validate()
    {
        try
        {
            CasualConnection con = connectionFactory.getConnection();
            con.close();

            // We just want to check that a connection could be established to check connectivity
            valid = true;
            LOG.finest(() -> "Successfully validated CasualConnection with jndiName=" + jndiName);
        }
        catch (ResourceException e)
        {
            // Failure to connect during validation should automatically invalidate ConnectionFactoryEntry
            valid = false;
            LOG.log(Level.WARNING, e, ()->"Failed validation of CasualConnection with jndiName=" + jndiName + ", received error: " + e.getMessage());
        }
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
