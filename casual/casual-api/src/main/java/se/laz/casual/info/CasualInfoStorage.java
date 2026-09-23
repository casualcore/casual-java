/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.info;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Static instance that contains information about inbound Casual Jca, services and queues.
 * Used primarily by casual-java cli.
 */
public class CasualInfoStorage
{
    private static final Logger LOG = Logger.getLogger( CasualInfoStorage.class.getName() );
    private final Map<String, List<Service>> services;
    private final Map<ServiceDescriptor, EventServiceStatistics> serviceStatistics;

    CasualInfoStorage()
    {
        this.services = new ConcurrentHashMap<>();
        this.serviceStatistics = new ConcurrentHashMap<>();
    }

    /**
     * Get all for service name
     *
     * @return List of services
     */
    List<Service> getService(String serviceName)
    {
        return services.getOrDefault(serviceName, Collections.emptyList());
    }

    public List<Service> getServices()
    {
        List<Service> allServices = new ArrayList<>();
        services.forEach((s, serviceList) -> allServices.addAll(serviceList));
        return Collections.unmodifiableList(allServices);
    }

    /**
     * Get service statistics
     *
     * @param serviceDescriptor - composite key of service name and service order
     * @return Optional EventServiceStatistics
     */
    Optional<EventServiceStatistics> getServiceStatistic( ServiceDescriptor serviceDescriptor )
    {
        return Optional.ofNullable( serviceStatistics.get( serviceDescriptor ) );
    }

    /**
     * Add/update service in storage
     *
     * @param service - service to be added/updated
     */
    synchronized void putService( Service service )
    {
        Objects.requireNonNull( service, "service must not be null" );
        if ( services.containsKey( service.getName() ) )
        {
            List<Service> serviceList = services.get(service.getName());
            Optional<Service> found = serviceList.stream().findFirst().filter(s -> s.matches(service));
            found.ifPresent(serviceList::remove);
            serviceList.add(service);
        }
        else
        {
            List<Service> s = new ArrayList<>();
            s.add(service);
            services.put(service.getName(), Collections.synchronizedList( s ) );
        }
    }

    /**
     * Add service statistics
     *
     * @param serviceName - service name
     * @param order       - order of service
     * @param start       - start timestamp of event
     * @param end         - end timestamp of event
     */
    synchronized void putEvent( String serviceName, char order, long start, long end )
    {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor( serviceName, Order.unmarshall( order ) );
        EventServiceStatistics event = toEvent( serviceDescriptor, start, end );
        serviceStatistics.put( serviceDescriptor, event );
        LOG.finest( () -> "Stored statistics: '%s'.".formatted( event ) );
    }

    /**
     * Creates or updates statistics for a specific service (inbound or outbound).
     *
     * @param serviceDescriptor - service descriptor
     * @param start             - start time
     * @param end               -  end time
     * @return EventServiceStatistics
     */
    private EventServiceStatistics toEvent( ServiceDescriptor serviceDescriptor, long start, long end )
    {
        EventServiceStatistics eventServiceStatistics = getServiceStatistic( serviceDescriptor )
                .orElseGet( () -> new EventServiceStatistics.Builder().name( serviceDescriptor.name() ).order( serviceDescriptor.order().getValue() ).build() );
        EventServiceStatistics.Builder builder = EventServiceStatistics.newBuilder( eventServiceStatistics );
        long executionTime = end - start;
        builder.increment();
        builder.max( executionTime );
        builder.min( executionTime );
        builder.last( start );
        builder.increaseTotal( executionTime );
        return builder.build();
    }

    void clear()
    {
        services.clear();
        serviceStatistics.clear();
    }
}
