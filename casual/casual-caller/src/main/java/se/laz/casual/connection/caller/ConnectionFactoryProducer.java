/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.jca.CasualConnectionFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Objects;

public class ConnectionFactoryProducer
{

    private final String jndiName;
    private ConnectionFactoryProducer(String jndiName)
    {
        this.jndiName = jndiName;
    }

    public static ConnectionFactoryProducer of(String jndiName)
    {
        Objects.requireNonNull(jndiName, "jndiName can not be null");
        return new ConnectionFactoryProducer(jndiName);
    }

    public String getJndiName()
    {
        return jndiName;
    }

    public CasualConnectionFactory getConnectionFactory()
    {
        try
        {
            InitialContext context = new InitialContext();
            return (CasualConnectionFactory)context.lookup(jndiName);
        }
        catch (NamingException e)
        {
            throw new CasualResourceException("Lookup failed for: " + jndiName, e);
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
        ConnectionFactoryProducer that = (ConnectionFactoryProducer) o;
        return Objects.equals(jndiName, that.jndiName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(jndiName);
    }

    @Override
    public String toString()
    {
        return "CasualConnectionFactoryProducer{" +
                "jndiName='" + jndiName + '\'' +
                '}';
    }
}
