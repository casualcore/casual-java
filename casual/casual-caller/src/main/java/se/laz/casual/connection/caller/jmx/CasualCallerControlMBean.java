/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller.jmx;

import java.util.List;

public interface CasualCallerControlMBean
{
    void purgeServiceCache();
    void purgeQueueCache();

    List<String> cachedServices();
    List<String> cachedQueues();

    List<String> factoriesChecked(String serviceName);
    List<String> factoriesContaining(String serviceName);
}
