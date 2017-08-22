package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.ServiceReturn;
import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.network.connection.CasualConnectionException;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * CasualConnectionImpl - handle object for a connection
 * Managed by a ManagedConnection
 * @see CasualManagedConnection
 * @version $Revision: $
 */
public class CasualConnectionImpl implements CasualConnection
{
    /**
     * The logger
     */
    private static Logger log = Logger.getLogger(CasualConnectionImpl.class.getName());

    /**
     * ManagedConnection
     */
    private CasualManagedConnection mc;

    /**
     * ManagedConnectionFactory
     */
    private CasualManagedConnectionFactory mcf;


    /**
     * Default constructor
     *
     * @param mc  CasualManagedConnection
     * @param mcf CasualManagedConnectionFactory
     */
    public CasualConnectionImpl(CasualManagedConnection mc, CasualManagedConnectionFactory mcf)
    {
        this.mc = mc;
        this.mcf = mcf;
    }

    public void invalidate()
    {
        mc = null;
    }

    public boolean isInvalid()
    {
        return null == mc;
    }

    @Override
    public void close()
    {
        if(isInvalid())
        {
            throw new CasualConnectionException("connection is invalidated!");
        }
        mc.closeHandle(this);
    }

    @Override
    public <X extends CasualBuffer> ServiceReturn<X> tpcall(String serviceName, X data, Flag flags, Class<X> bufferClass)
    {
        if(isInvalid())
        {
            throw new CasualConnectionException("connection is invalidated!");
        }
        return mc.tpcall(serviceName, data, flags, bufferClass);
    }

    @Override
    public <X extends CasualBuffer> CompletableFuture<ServiceReturn<X>> tpacall(String serviceName, X data, Flag flags, Class<X> bufferClass)
    {
        if(isInvalid())
        {
            throw new CasualConnectionException("connection is invalidated!");
        }
        throw new CasualConnectionException("not yet implemented");
    }

    void setManagedConnection(CasualManagedConnection mc)
    {
        this.mc = mc;
    }
    CasualManagedConnection getManagedConnection()
    {
        return mc;
    }
}
