package se.kodarkatten.casual.jca;

import java.io.Serializable;

import javax.resource.Referenceable;
import javax.resource.ResourceException;

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
    * @return CasualConnection instance
    * @exception ResourceException Thrown if a connection can't be obtained
    */
   CasualConnection getConnection() throws ResourceException;

}
