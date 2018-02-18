package se.kodarkatten.casual.jca;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ConnectionFactoryDefinition;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.TransactionSupport;
import java.util.Objects;
import java.util.logging.Logger;

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

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(CasualConnectionFactoryImpl.class.getName());
    private Reference reference;
    private CasualManagedConnectionFactory managedConnectionFactory;
    private ConnectionManager connectionManager;

    /**
     * NetworkConnection Factory instance is created from the {@link javax.resource.spi.ManagedConnectionFactory}
     * as part of the Application Server lifecycle.
     * <br/>
     * When created a reference to the resources created by the application server
     * are passed to allow future interactions with them when needed.
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
        log.finest("getConnection()");
        return (CasualConnection) connectionManager.allocateConnection(managedConnectionFactory, null);
    }

    @Override
    public Reference getReference() throws NamingException
    {
        log.finest("getReference()");
        return reference;
    }

    @Override
    public void setReference(Reference reference)
    {
        log.finest("setReference()");
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
