/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

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
