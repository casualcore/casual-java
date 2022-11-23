/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller.jmx;

import java.util.List;

public interface CasualCallerControlMBean
{
    List<String> validPools();
    List<String> invalidPools();

    void purgeServiceCache();
    List<String> cachedServices();
    List<String> poolsCheckedForService(String serviceName);
    List<String> poolsContainingService(String serviceName);

    void purgeQueueCache();
    List<String> cachedQueues();
    List<String> queueInPools(String queueName);
    String getQueueStickiedPool(String queueName);

    boolean transactionStickyEnabled();
}
