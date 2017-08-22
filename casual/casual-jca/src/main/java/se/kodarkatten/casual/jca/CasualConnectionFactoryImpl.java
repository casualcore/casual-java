package se.kodarkatten.casual.jca;

import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;

import javax.resource.ConnectionFactoryDefinition;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.TransactionSupport;

/**
 * CasualConnectionFactoryImpl
 *
 * @version $Revision: $
 */
@ConnectionFactoryDefinition(name = "java:/eis/CasualConnectionFactory",
        interfaceName = "se.kodarkatten.casual.jca.CasualConnectionFactory",
        resourceAdapter = "se.kodarkatten.casual.jca.CasualResourceAdapter",
        description = "Casual Middleware Resource Adaptter with XA",
        transactionSupport = TransactionSupport.TransactionSupportLevel.XATransaction)
public class CasualConnectionFactoryImpl implements CasualConnectionFactory
{
    /**
     * The serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The logger
     */
    private static Logger log = Logger.getLogger(CasualConnectionFactoryImpl.class.getName());

    /**
     * Reference
     */
    private Reference reference;

    /**
     * ManagedConnectionFactory
     */
    private CasualManagedConnectionFactory mcf;

    /**
     * ConnectionManager
     */
    private ConnectionManager connectionManager;

    /**
     * Default constructor
     */
    public CasualConnectionFactoryImpl()
    {

    }

    /**
     * Default constructor
     *
     * @param mcf       ManagedConnectionFactory
     * @param cxManager ConnectionManager
     */
    public CasualConnectionFactoryImpl(CasualManagedConnectionFactory mcf, ConnectionManager cxManager)
    {
        this.mcf = mcf;
        this.connectionManager = cxManager;
    }

    /**
     * Get connection from factory
     *
     * @return CasualConnection instance
     * @throws ResourceException Thrown if a connection can't be obtained
     */
    @Override
    public CasualConnection getConnection() throws ResourceException
    {
        log.finest("getConnection()");
        return (CasualConnection) connectionManager.allocateConnection(mcf, null);
    }

    /**
     * Get the Reference instance.
     *
     * @return Reference instance
     * @throws NamingException Thrown if a reference can't be obtained
     */
    @Override
    public Reference getReference() throws NamingException
    {
        log.finest("getReference()");
        return reference;
    }

    /**
     * Set the Reference instance.
     *
     * @param reference A Reference instance
     */
    @Override
    public void setReference(Reference reference)
    {
        log.finest("setReference()");
        this.reference = reference;
    }


}
