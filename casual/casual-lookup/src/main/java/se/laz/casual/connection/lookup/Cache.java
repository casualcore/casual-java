/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.lookup;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;

import javax.ejb.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class Cache
{
    private final Map<QueueInfo, String> queueJNDINames = new ConcurrentHashMap<>();
    private final Map<ServiceInfo, String> serviceJNDINames = new ConcurrentHashMap<>();

    public Optional<String> getJNDIName(ServiceInfo serviceInfo)
    {
        return Optional.ofNullable(serviceJNDINames.get(serviceInfo));
    }

    public Optional<String> getJNDIName(QueueInfo qinfo)
    {
        return Optional.ofNullable(queueJNDINames.get(qinfo));
    }

    public void setJNDIName(ServiceInfo serviceInfo, String jndi)
    {
        Objects.requireNonNull(serviceInfo, "serviceInfo can not be null");
        Objects.requireNonNull(jndi, "jndi can not be null");
        serviceJNDINames.put(serviceInfo, jndi);
    }

    public void setJNDIName(QueueInfo qinfo, String jndi)
    {
        Objects.requireNonNull(qinfo, "qinfo can not be null");
        Objects.requireNonNull(jndi, "jndi can not be null");
        queueJNDINames.put(qinfo, jndi);
    }

    public void evict(QueueInfo qinfo)
    {
        queueJNDINames.remove(qinfo);
    }

    public void evict(ServiceInfo serviceInfo)
    {
        serviceJNDINames.remove(serviceInfo);
    }

}
