/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller.jmx;

import java.util.Collections;
import java.util.List;

public class CasualCallerControl implements CasualCallerControlMBean
{
    public static CasualCallerControl of()
    {
        return new CasualCallerControl();
    }

    @Override
    public List<String> validPools()
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> invalidPools()
    {
        return Collections.emptyList();
    }


    @Override
    public void purgeServiceCache()
    {
    }

    @Override
    public void purgeQueueCache()
    {

    }

    @Override
    public List<String> cachedServices()
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> cachedQueues()
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> poolsCheckedForService(String serviceName)
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> poolsContainingService(String serviceName)
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> queueInPools(String queueName)
    {
        return Collections.emptyList();
    }

    @Override
    public String getQueueStickiedPool(String queueName)
    {
        return null;
    }
}
