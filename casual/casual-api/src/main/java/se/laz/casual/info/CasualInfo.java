package se.laz.casual.info;

import se.laz.casual.api.service.ServiceDetails;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Static instance that stores information about Casual jca, services and queues
 * in {@link CasualInfoStorage}.
 * Used primarily by casual-java cli.
 */
public final class CasualInfo
{
    private static final Logger LOG = Logger.getLogger( CasualInfo.class.getName() );
    private static final CasualInfo instance = new CasualInfo();

    private CasualInfo()
    {
        // no-op
    }

    public static CasualInfo getInstance()
    {
        return instance;
    }

    /**
     * Add an inbound service to storage
     *
     * @param service - a service
     */
    public void addService( Service service )
    {
        Objects.requireNonNull( service, "service must not be null" );
        CasualInfoStorage.getInstance().putService( service );
    }

    /**
     * Add an outbound service to storage
     *
     * @param serviceDetailsList - details about the service
     * @param connection         - service connection details
     */
    public void addDiscovery( List<ServiceDetails> serviceDetailsList, Connection connection )
    {
        serviceDetailsList.forEach( serviceDetails -> addDiscoveredService( serviceDetails, connection ) );
    }

    /**
     * Store and/or update statistics for a service.
     * Should only be called when an event is created from a service call.
     *
     * @param serviceName - service name
     * @param order       - order of service
     * @param start       - start time
     * @param end         - end time
     */
    public void storeEvent( String serviceName, char order, long start, long end )
    {
        EventServiceStatistics eventServiceStatistics = toEvent( serviceName, order, start, end );
        CasualInfoStorage.getInstance().putEvent( new ServiceDescriptor( eventServiceStatistics.getName(),
                Order.unmarshall( order ) ), eventServiceStatistics );
        LOG.finest( () -> "Stored statistics: '%s'.".formatted( eventServiceStatistics ) );
    }

    /**
     * Adds an outbound service to storage
     *
     * @param serviceDetails - service details
     * @param connection     - service connection details
     */
    private void addDiscoveredService( ServiceDetails serviceDetails, Connection connection )
    {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor( serviceDetails.getName(), Order.CONCURRENT );
        Optional<Service> service = CasualInfoStorage.getInstance().getService( serviceDescriptor );
        Service.Builder builder = service.isPresent() ? Service.newBuilder( service.get() ) :
                Service.newBuilder( serviceDetails, Order.CONCURRENT );
        builder.connection( connection );
        CasualInfoStorage.getInstance().putService( builder.build() );
    }

    /**
     * Creates or updates statistics for a specific service (inbound or outbound).
     *
     * @param serviceName - service
     * @param order       - order of service e.g. inbound or outbound
     * @param start       - start time
     * @param end         -  end time
     * @return EventServiceStatistics
     */
    private EventServiceStatistics toEvent( String serviceName, char order, long start, long end )
    {
        EventServiceStatistics eventServiceStatistics =
                CasualInfoStorage.getInstance().getServiceStatistic( new ServiceDescriptor( serviceName,
                                Order.unmarshall( order ) ) )
                        .orElseGet( () -> new EventServiceStatistics.Builder().name( serviceName ).order( order ).build() );
        EventServiceStatistics.Builder builder = EventServiceStatistics.newBuilder( eventServiceStatistics );
        long executionTime = end - start;
        builder.increment();
        builder.max( executionTime );
        builder.min( executionTime );
        builder.last( start );
        builder.increaseTotal( executionTime );
        return builder.build();
    }
}
