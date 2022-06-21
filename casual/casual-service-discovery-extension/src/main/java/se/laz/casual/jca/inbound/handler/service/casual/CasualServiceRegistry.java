/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Internal Registry for discovered EJB endpoints with CasualService annotation to allow inbound requests to be dispatched.
 */
public final class CasualServiceRegistry
{
    private final Map<String,CasualServiceMetaData> serviceMetaData;
    private final Map<String,CasualServiceEntry> serviceEntries;

    private static final CasualServiceRegistry instance = new CasualServiceRegistry();

    private CasualServiceRegistry()
    {
        serviceMetaData = new ConcurrentHashMap<>();
        serviceEntries = new ConcurrentHashMap<>();
    }

    public static CasualServiceRegistry getInstance()
    {
        return instance;
    }

    public void register( CasualServiceMetaData metaData )
    {
        serviceMetaData.put( metaData.getServiceName(), metaData );
    }

    public void register( CasualServiceEntry entry )
    {
        serviceMetaData.get( entry.getServiceName() ).setResolvedEntry( entry );
        serviceEntries.put( entry.getServiceName(), entry );
    }

    public boolean hasServiceMetaData(String name )
    {
        return serviceMetaData.containsKey( name );
    }

    public boolean hasServiceEntry( String name )
    {
        return serviceEntries.containsKey( name );
    }

    public CasualServiceMetaData getServiceMetaData(String name )
    {
        return serviceMetaData.get( name );
    }

    public CasualServiceEntry getServiceEntry( String name )
    {
        return serviceEntries.get( name );
    }

    public int serviceMetaDataSize()
    {
        return serviceMetaData.size();
    }
    public int serviceEntrySize()
    {
        return serviceEntries.size();
    }

    public List<CasualServiceMetaData> getUnresolvedServices()
    {
        return serviceMetaData.values().stream().filter(CasualServiceMetaData::isUnresolved).collect(Collectors.toList());
    }

    public Map<String,CasualServiceEntry> getServiceEntries()
    {
        return Collections.unmodifiableMap(serviceEntries);
    }

    public List<String> getServices()
    {
        return getServiceEntries().keySet().stream()
                                  .collect(Collectors.toList());
    }

    public void clear()
    {
        this.serviceMetaData.clear();
        this.serviceEntries.clear();
    }



}
