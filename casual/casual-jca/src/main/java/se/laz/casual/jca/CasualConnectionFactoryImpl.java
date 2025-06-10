/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import javax.naming.NamingException;
import javax.naming.Reference;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import java.util.Objects;

import static java.lang.System.Logger.Level.*;

/**
 * CasualConnectionFactoryImpl
 *
 * @version $Revision: $
 */
public class CasualConnectionFactoryImpl implements CasualConnectionFactory
{

    private static final long serialVersionUID = 1L;
    private static System.Logger log = System.getLogger(CasualConnectionFactoryImpl.class.getName());
    private Reference reference;
    private CasualManagedConnectionFactory managedConnectionFactory;
    private ConnectionManager connectionManager;

    /**
     * NetworkConnection Factory instance is created from the {@link jakarta.resource.spi.ManagedConnectionFactory}
     * as part of the Application Server lifecycle.
     *
     * <p>When created a reference to the resources created by the application server
     * are passed to allow future interactions with them when needed.</p>
     *
     * @param mcf       ManagedConnectionFactory
     * @param cxManager ConnectionManager
     */
    public CasualConnectionFactoryImpl(CasualManagedConnectionFactory mcf, ConnectionManager cxManager)
    {
        this.managedConnectionFactory = mcf;
        this.connectionManager = cxManager;
    }

    @Override
    public CasualConnection getConnection() throws ResourceException
    {
        log.log(DEBUG,"getConnection()");
        return getConnection(null);
    }

    @Override
    public CasualConnection getConnection(ConnectionRequestInfo connectionRequestInfo) throws ResourceException
    {
        log.log(DEBUG,"getConnection()");
        return (CasualConnection) connectionManager.allocateConnection(managedConnectionFactory, connectionRequestInfo);
    }

    @Override
    public Reference getReference() throws NamingException
    {
        log.log(DEBUG,"getReference()");
        return reference;
    }

    @Override
    public void setReference(Reference reference)
    {
        log.log(DEBUG,"setReference()");
        this.reference = reference;
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
        CasualConnectionFactoryImpl that = (CasualConnectionFactoryImpl) o;
        return Objects.equals(reference, that.reference) &&
                Objects.equals(managedConnectionFactory, that.managedConnectionFactory) &&
                Objects.equals(connectionManager, that.connectionManager);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(reference, managedConnectionFactory, connectionManager);
    }

    @Override
    public String toString()
    {
        return "CasualConnectionFactoryImpl{" +
                "reference=" + reference +
                ", managedConnectionFactory=" + managedConnectionFactory +
                ", connectionManager=" + connectionManager +
                '}';
    }
}
