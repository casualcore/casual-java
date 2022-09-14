/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller.jmx;

import java.util.List;

public interface CasualCallerControlMBean
{
    List<String> connectionFactoryJNDINames();
    List<String> currentPools();

    void purgeServiceCache();
    List<String> cachedServices();
    List<String> allServices();
    List<String> cacheEntryForService(String serviceName);

    void purgeQueueCache();
    List<String> cachedQueues();
    List<String> allQueues();
    String cacheEntryForQueue(String queueName);
}
