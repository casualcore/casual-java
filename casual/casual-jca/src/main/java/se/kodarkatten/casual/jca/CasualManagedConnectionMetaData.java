/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.internal.CasualConstants;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * CasualManagedConnectionMetaData
 *
 * @version $Revision: $
 */
public final class CasualManagedConnectionMetaData implements ManagedConnectionMetaData
{
    /**
     * The logger
     */
    private static Logger log = Logger.getLogger(CasualManagedConnectionMetaData.class.getName());

    /**
     * Default constructor
     */
    public CasualManagedConnectionMetaData()
    {

    }

    /**
     * Returns Product name of the underlying EIS instance connected through the ManagedConnection.
     *
     * @return Product name of the EIS instance
     * @throws ResourceException Thrown if an error occurs
     */
    @Override
    public String getEISProductName() throws ResourceException
    {
        log.finest("getEISProductName()");
        return CasualConstants.CASUAL_NAME;
    }

    /**
     * Returns the API version used to communicate
     *
     * @return API version of the EIS instance
     * @throws ResourceException Thrown if an error occurs
     */
    @Override
    public String getEISProductVersion() throws ResourceException
    {
        log.finest("getEISProductVersion()");
        return CasualConstants.CASUAL_API_VERSION;
    }

    /**
     * Returns maximum limit on number of active concurrent connections
     *
     * @return Maximum limit for number of active concurrent connections
     * @throws ResourceException Thrown if an error occurs
     */
    @Override
    public int getMaxConnections() throws ResourceException
    {
        log.finest("getMaxConnections()");
        return Runtime.getRuntime().availableProcessors() - 1; //TODO How to determine this? Using core count on machine-1 for now
    }

    /**
     * Returns name of the user associated with the ManagedConnection instance
     *
     * @return Name of the user
     * @throws ResourceException Thrown if an error occurs
     */
    @Override
    public String getUserName() throws ResourceException
    {
        log.finest("getUserName()");
        return System.getProperty("user.name");
    }
}
