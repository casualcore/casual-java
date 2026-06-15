/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CasualInfoStorage
{
    private static final CasualInfoStorage instance = new CasualInfoStorage();
    private final Map<ServiceDescriptor, List<Service>> services;
    private final Map<ServiceDescriptor, EventServiceStatistics> serviceStatistics;

    private CasualInfoStorage()
    {
        this.services = new ConcurrentHashMap<>();
        this.serviceStatistics = new ConcurrentHashMap<>();
    }

    public static CasualInfoStorage getInstance()
    {
        return instance;
    }

    public Optional<Service> getService( ServiceDescriptor serviceDescriptor )
    {
        Objects.requireNonNull( serviceDescriptor, "serviceDescriptor must not be null" );
        List<Service> serviceList = instance.services.getOrDefault( serviceDescriptor,
                Collections.emptyList() );
        return !serviceList.isEmpty() ? Optional.of( serviceList.get( 0 ) ) : Optional.empty();
    }

    public List<Service> getServices()
    {
        List<Service> servicesList = new ArrayList<>();
        instance.services.values().forEach( servicesList::addAll );
        return Collections.unmodifiableList( servicesList );
    }

    public Optional<EventServiceStatistics> getServiceStatistic( ServiceDescriptor serviceDescriptor )
    {
        return Optional.ofNullable( instance.serviceStatistics.get( serviceDescriptor ) );
    }

    protected void putService( Service service )
    {
        Objects.requireNonNull( service, "service must not be null" );
        services.put( new ServiceDescriptor( service.getName(), service.getOrder() ),
                Collections.synchronizedList( List.of( service ) ) );
    }

    protected void putEvent( ServiceDescriptor serviceDescriptor, EventServiceStatistics eventServiceStatistics )
    {
        Objects.requireNonNull( serviceDescriptor, "serviceDescriptor must not be null" );
        serviceStatistics.put( serviceDescriptor, eventServiceStatistics );
    }
}
