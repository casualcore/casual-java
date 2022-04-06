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

public class ConnectionFactoryEntry
{
    private static final Logger LOG = Logger.getLogger(ConnectionFactoryEntry.class.getName());
    private final ConnectionFactoryProducer connectionFactoryProducer;

    /**
     * Connection factory entries should invalidate on connection errors and revalidate as soon as a new valid
     * connection can be established.
     */
    private boolean valid = true;

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
        LOG.finest(() -> "Invalidated CasualConnection with jndiName=" + connectionFactoryProducer.getJndiName());
    }

    //Note: due to try with resources usage where we never use the resource
    @SuppressWarnings("try")
    public void validate()
    {
        try(CasualConnection con = getConnectionFactory().getConnection())
        {
            // We just want to check that a connection could be established to check connectivity
            valid = true;
            LOG.finest(() -> "Successfully validated CasualConnection with jndiName=" + connectionFactoryProducer.getJndiName());
        }
        catch (ResourceException e)
        {
            // Failure to connect during validation should automatically invalidate ConnectionFactoryEntry
            valid = false;
            LOG.log(Level.WARNING, e, ()->"Failed validation of CasualConnection with jndiName=" + connectionFactoryProducer.getJndiName() + ", received error: " + e.getMessage());
        }
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
        return isValid() == that.isValid() && connectionFactoryProducer.equals(that.connectionFactoryProducer);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(connectionFactoryProducer, isValid());
    }

    @Override
    public String toString()
    {
        return "ConnectionFactoryEntry{" +
                "connectionFactoryProducer=" + connectionFactoryProducer +
                ", valid=" + valid +
                '}';
    }
}
