package se.laz.casual.info;

import se.laz.casual.api.service.ServiceDetails;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Static instance that contains information about inbound Casual Services.
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

    public void addService( Service service )
    {
        Objects.requireNonNull( service, "service must not be null" );
        CasualInfoStorage.getInstance().putService( service );
    }

    public void addDiscovery( List<ServiceDetails> serviceDetailsList, Connection connection )
    {
        serviceDetailsList.forEach( serviceDetails -> addDiscoveredService( serviceDetails, connection ) );
    }

    public void storeEvent( String serviceName, char order, long start, long end )
    {
        EventServiceStatistics eventServiceStatistics = toEvent( serviceName, order, start, end );
        CasualInfoStorage.getInstance().putEvent( new ServiceDescriptor( eventServiceStatistics.getName(),
                Order.unmarshall( order ) ), eventServiceStatistics );
        LOG.finest( () -> "Stored statistics: '%s'.".formatted( eventServiceStatistics ) );
    }

    private void addDiscoveredService( ServiceDetails serviceDetails, Connection connection )
    {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor( serviceDetails.getName(), Order.CONCURRENT );
        Optional<Service> service = CasualInfoStorage.getInstance().getService( serviceDescriptor );
        Service.Builder builder = service.isPresent() ? Service.newBuilder( service.get() ) :
                Service.newBuilder( serviceDetails, Order.CONCURRENT );
        builder.connection( connection );
        CasualInfoStorage.getInstance().putService( builder.build() );
    }

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
