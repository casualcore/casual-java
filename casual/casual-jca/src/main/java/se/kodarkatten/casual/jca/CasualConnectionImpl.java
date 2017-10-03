package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.ServiceReturn;
import se.kodarkatten.casual.api.flags.AtmiFlags;
import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.jca.service.CasualServiceCaller;
import se.kodarkatten.casual.network.connection.CasualConnectionException;

import java.util.concurrent.CompletableFuture;

/**
 * CasualConnectionImpl - handle object for a connection
 * Managed by a ManagedConnection
 * @see CasualManagedConnection
 * @version $Revision: $
 */
public class CasualConnectionImpl implements CasualConnection
{
    private CasualServiceCaller serviceCaller;
    private CasualManagedConnection managedConnection;

    /**
     * Create a connection handle with a reference to the underlying managed connection
     * created by the Application Server.
     *
     * @param mc  CasualManagedConnection
     */
    public CasualConnectionImpl(CasualManagedConnection mc)
    {
        this.managedConnection = mc;
    }

    /**
     * Invalidate this connection handle removing its reference to
     * the underlying {@link javax.resource.spi.ManagedConnection}.
     */
    public void invalidate()
    {
        managedConnection = null;
    }

    /**
     * Is this connection handle valid i.e. associated with a managed connection.
     *
     * @return invalid true or valid false.
     */
    public boolean isInvalid()
    {
        return null == managedConnection;
    }

    @Override
    public void close()
    {
        throwIfInvalidated();
        managedConnection.closeHandle(this);
    }

    @Override
    public <X extends CasualBuffer> ServiceReturn<X> tpcall(String serviceName, X data, Flag<AtmiFlags> flags, Class<X> bufferClass)
    {
        throwIfInvalidated();
        return getCasualServiceCaller().tpcall( serviceName, data, flags, bufferClass );
    }

    @Override
    public <X extends CasualBuffer> CompletableFuture<ServiceReturn<X>> tpacall(String serviceName, X data, Flag<AtmiFlags> flags, Class<X> bufferClass)
    {
        throwIfInvalidated();
        return getCasualServiceCaller().tpacall( serviceName, data, flags, bufferClass );
    }

    private void throwIfInvalidated()
    {
        if(isInvalid())
        {
            throw new CasualConnectionException("connection is invalidated!");
        }
    }

    /**
     * Get the {@link CasualManagedConnection} to which this handle refers.
     *
     * @return current reference managed connection or null, if invalidated.
     */
    public CasualManagedConnection getManagedConnection()
    {
        return managedConnection;
    }

    /**
     * Set the {@link CasualManagedConnection} to which this handle refers.
     *
     * @param managedConnection managed connection to which this refers.
     */
    public void setManagedConnection(CasualManagedConnection managedConnection)
    {
        this.managedConnection = managedConnection;
    }

    CasualServiceCaller getCasualServiceCaller()
    {
        if( serviceCaller == null )
        {
            return CasualServiceCaller.of( getManagedConnection() );
        }
        return serviceCaller;
    }

    void setCasualServiceCaller( CasualServiceCaller serviceCaller )
    {
        this.serviceCaller  = serviceCaller;
    }

    @Override
    public String toString()
    {
        return "CasualConnectionImpl{" +
                "managedConnection=" + managedConnection +
                '}';
    }
}
