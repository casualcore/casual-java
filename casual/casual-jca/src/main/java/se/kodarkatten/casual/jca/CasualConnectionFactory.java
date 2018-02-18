package se.kodarkatten.casual.jca;

import javax.resource.Referenceable;
import javax.resource.ResourceException;
import java.io.Serializable;

/**
 * CasualConnectionFactory
 *
 * @version $Revision: $
 */
public interface CasualConnectionFactory extends Serializable, Referenceable
{
   /** 
    * Get connection from factory
    *
    * @return DefaultNetworkConnection instance
    * @exception ResourceException Thrown if a connection can't be obtained
    */
   CasualConnection getConnection() throws ResourceException;

}
