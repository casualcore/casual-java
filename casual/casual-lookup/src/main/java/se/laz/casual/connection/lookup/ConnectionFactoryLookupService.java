/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.lookup;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;

import javax.ejb.DependsOn;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@DependsOn({"ConfigurationProvider", "Cache"})
@Remote(ConnectionFactoryLookup.class)
@Stateless
public class ConnectionFactoryLookupService implements ConnectionFactoryLookup
{
    @Inject
    private ConfigurationProvider config;
    @Inject
    private Cache cache;
    @Inject
    private Lookup lookup;
    private static final Map<?,?> noProperties = new HashMap<>();

    @Override
    public Optional<String> getJNDIName(QueueInfo qinfo, Map<?,?> initialContextEnvironment)
    {
        Objects.requireNonNull(qinfo, "qinfo can not be null");
        Objects.requireNonNull(initialContextEnvironment, "initialContextEnvironment can not be null");
        String cachedJNDIName = cache.getJNDIName(qinfo).orElse(null);
        if(cachedJNDIName != null)
        {
            return Optional.of(cachedJNDIName);
        }
        Optional<String> v = lookup.findJNDIName(qinfo, createContext(initialContextEnvironment), config.getCasualJNDINames());
        v.ifPresent(s -> cache.setJNDIName(qinfo, s));
        return v;
    }

    @Override
    public Optional<String> getJNDIName(QueueInfo qinfo)
    {
        return getJNDIName(qinfo, noProperties);
    }

    @Override
    public Optional<String> getJNDIName(ServiceInfo serviceInfo, Map<?,?> initialContextEnvironment)
    {
        Objects.requireNonNull(serviceInfo, "serviceInfo can not be null");
        Objects.requireNonNull(initialContextEnvironment, "initialContextEnvironment can not be null");
        String cachedJNDIName = cache.getJNDIName(serviceInfo).orElse(null);
        if(cachedJNDIName != null)
        {
            return Optional.of(cachedJNDIName);
        }
        Optional<String> v = lookup.findJNDIName(serviceInfo, createContext(initialContextEnvironment), config.getCasualJNDINames());
        v.ifPresent(s -> cache.setJNDIName(serviceInfo, s));
        return v;
    }

    @Override
    public Optional<String> getJNDIName(ServiceInfo serviceInfo)
    {
        return Optional.empty();
    }

    @Override
    public void evict(QueueInfo qinfo)
    {
        Objects.requireNonNull(qinfo, "qinfo is not allowed to be null");
        cache.evict(qinfo);
    }

    @Override
    public void evict(ServiceInfo serviceInfo)
    {
        Objects.requireNonNull(serviceInfo, "serviceInfo is not allowed to be null");
        cache.evict(serviceInfo);
    }

    private static InitialContext createContext(final Map<?,?> env)
    {
        try
        {
            return new InitialContext(mapToHashtable(env));
        }
        catch (NamingException e)
        {
            throw new CasualLookupException(e);
        }
    }

    // squid:S1149 - InitialContext only accepts Hashtable
    @SuppressWarnings("squid:S1149")
    private static <K,V> Hashtable<K,V> mapToHashtable(Map<K, V> env)
    {
        Hashtable<K,V> r = new Hashtable<>();
        r.putAll(env);
        return r;
    }

}
