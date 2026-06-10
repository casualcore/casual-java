package se.laz.casual.info;

import se.laz.casual.api.discovery.DiscoveryReturn;
import se.laz.casual.api.queue.QueueDetails;
import se.laz.casual.api.service.ServiceDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Static instance that contains information about inbound Casual Services.
 * Used primarily by casual-java cli.
 */
public final class CasualInfo
{
    private static Logger LOG = Logger.getLogger( CasualInfo.class.getName() );

    private static final CasualInfo instance = new CasualInfo();
    private final Map<String, Service> inboundServices;
    private final Map<String, List<Service>> outboundServices;
    private final Map<String, EventServiceStatistics> inboundStatistics;
    private final Map<String, EventServiceStatistics> outboundStatistics;

    private CasualInfo()
    {
        this.inboundServices = new ConcurrentHashMap<>();
        this.outboundServices = new ConcurrentHashMap<>();
        this.inboundStatistics = new ConcurrentHashMap<>();
        this.outboundStatistics = new ConcurrentHashMap<>();
    }

    public static void addInboundService( Service service )
    {
        Objects.requireNonNull( service, "service must not be null" );
        instance.inboundServices.put( service.getName(), service );
    }

    public static List<Service> getInboundServices()
    {
        return instance.inboundServices.values().stream().toList();
    }

    public static Optional<Service> getInboundService( String serviceName )
    {
        Objects.requireNonNull( serviceName, "name must not be null" );
        return Optional.ofNullable( instance.inboundServices.get( serviceName ) );
    }

    public static Optional<EventServiceStatistics> getInboundStatistic( String serviceName )
    {
        return Optional.ofNullable( instance.inboundStatistics.get( serviceName ) );
    }

    public static List<Service> getOutboundServices()
    {
        List<Service> outboundServices = new ArrayList<>();
        instance.outboundServices.values().forEach( outboundServices::addAll );
        return outboundServices;
    }

    public static List<Service> getOutboundService( String serviceName )
    {
        Objects.requireNonNull( serviceName, "name must not be null" );
        return instance.outboundServices.getOrDefault( serviceName, Collections.emptyList() );
    }

    public static Optional<EventServiceStatistics> getOutboundStatistic( String serviceName )
    {
        return Optional.ofNullable( instance.outboundStatistics.get( serviceName ) );
    }

    public static Map<String, EventServiceStatistics> getInboundStatistics()
    {
        return instance.inboundStatistics;
    }

    public static Map<String, EventServiceStatistics> getOutboundStatistics()
    {
        return instance.outboundStatistics;
    }

    public static void addDiscovery( DiscoveryReturn discoveryReturn, Connection connection )
    {
        discoveryReturn.getQueueDetails().forEach( queueDetails -> addOutboundQueue( queueDetails, connection ) );
        discoveryReturn.getServiceDetails().forEach( serviceDetails -> addOutboundService( serviceDetails,
                connection ) );
    }

    public static void addDiscovery( List<ServiceDetails> serviceDetailsList, Connection connection )
    {
        serviceDetailsList.forEach( serviceDetails -> addOutboundService( serviceDetails, connection ) );
    }

    public static void storeEvent( String serviceName, char order, long start, long end )
    {
        EventServiceStatistics eventServiceStatistics = instance.inboundStatistics.getOrDefault( serviceName,
                new EventServiceStatistics.Builder().name( serviceName )
                        .order( order ).build() );
        eventServiceStatistics.increment();
        long executionTime = end - start;
        eventServiceStatistics.setMin( executionTime );
        eventServiceStatistics.setMax( executionTime );
        eventServiceStatistics.setLast( end );
        eventServiceStatistics.increaseTotal( executionTime );
        if( order == 'S' )
        {
            instance.inboundStatistics.put( serviceName, eventServiceStatistics );
        }
        else
        {
            instance.outboundStatistics.put( serviceName, eventServiceStatistics );
        }
        LOG.finest( () -> "Stored statistics: '%s'.".formatted( eventServiceStatistics ) );
    }

    private static void addOutboundService( ServiceDetails serviceDetails, Connection connection )
    {
        Service service = new Service.Builder().newBuilder( serviceDetails )
                .connection( connection ).build();
        String domainId = connection.getDomainId().getId().toString();
        List<Service> allDiscoveries = instance.outboundServices.getOrDefault( domainId,
                Collections.synchronizedList( new ArrayList<>() ) );
        Optional<Service> exists = allDiscoveries.stream().findFirst().filter( s -> s.equals( service ) );
        if( exists.isEmpty() )
        {
            allDiscoveries.add( service );
            instance.outboundServices.put( domainId, allDiscoveries );
        }
    }

    private static void addOutboundQueue( QueueDetails queueDetails, Connection connection )
    {
        //Not implemented!
    }
}
