/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import se.laz.casual.internal.CasualConstants;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;
import java.util.logging.Logger;

/**
 * CasualManagedConnectionMetaData
 *
 * @version $Revision: $
 */
public final class CasualManagedConnectionMetaData implements ManagedConnectionMetaData
{
    private static Logger log = Logger.getLogger(CasualManagedConnectionMetaData.class.getName());

    @Override
    public String getEISProductName() throws ResourceException
    {
        log.finest("getEISProductName()");
        return CasualConstants.CASUAL_NAME;
    }

    @Override
    public String getEISProductVersion() throws ResourceException
    {
        log.finest("getEISProductVersion()");
        return CasualConstants.CASUAL_API_VERSION;
    }

    @Override
    public int getMaxConnections() throws ResourceException
    {
        log.finest("getMaxConnections()");
        //Assuming this is the EIS Server we are not aware of any limitations
        //so shall, to that effect, return 0.
        return 0;
    }

    @Override
    public String getUserName() throws ResourceException
    {
        log.finest("getUserName()");
        return System.getProperty("user.name");
    }



}
