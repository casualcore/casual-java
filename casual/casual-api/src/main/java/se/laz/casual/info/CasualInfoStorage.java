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

/**
 * Static instance that contains information about inbound Casual Jca, services and queues.
 * Used primarily by casual-java cli.
 */
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

    /**
     * Get service from storage if it exists
     *
     * @param serviceDescriptor - composite key of service name and service order
     * @return Service
     */
    public Optional<Service> getService( ServiceDescriptor serviceDescriptor )
    {
        Objects.requireNonNull( serviceDescriptor, "serviceDescriptor must not be null" );
        List<Service> serviceList = instance.services.getOrDefault( serviceDescriptor,
                Collections.emptyList() );
        return !serviceList.isEmpty() ? Optional.of( serviceList.get( 0 ) ) : Optional.empty();
    }

    /**
     * Get all services
     *
     * @return List of services
     */
    public List<Service> getServices()
    {
        List<Service> servicesList = new ArrayList<>();
        instance.services.values().forEach( servicesList::addAll );
        return Collections.unmodifiableList( servicesList );
    }

    /**
     * Get service statistics
     *
     * @param serviceDescriptor - composite key of service name and service order
     * @return Optional EventServiceStatistics
     */
    public Optional<EventServiceStatistics> getServiceStatistic( ServiceDescriptor serviceDescriptor )
    {
        return Optional.ofNullable( instance.serviceStatistics.get( serviceDescriptor ) );
    }

    /**
     * Add/update service in storage
     *
     * @param service - service to be added/updated
     */
    protected void putService( Service service )
    {
        Objects.requireNonNull( service, "service must not be null" );
        services.put( new ServiceDescriptor( service.getName(), service.getOrder() ),
                Collections.synchronizedList( List.of( service ) ) );
    }

    /**
     * Add service statistics
     *
     * @param serviceDescriptor - composite key of service name and service order
     * @param eventServiceStatistics - event statistics for service
     */
    protected void putEvent( ServiceDescriptor serviceDescriptor, EventServiceStatistics eventServiceStatistics )
    {
        Objects.requireNonNull( serviceDescriptor, "serviceDescriptor must not be null" );
        serviceStatistics.put( serviceDescriptor, eventServiceStatistics );
    }
}
