package se.laz.casual.info;

import se.laz.casual.api.service.ServiceDetails;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Static instance that stores information about Casual jca, services and queues
 * in {@link CasualInfoStorage}.
 * Used primarily by casual-java cli.
 */
public final class CasualInfo
{
    private static final CasualInfo instance = new CasualInfo();
    private static final CasualInfoStorage storageInstance = CasualInfoStorage.getInstance();

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
        storageInstance.putService( service );
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
        storageInstance.putEvent( serviceName, order, start, end );
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
        Optional<Service> service = storageInstance.getService( serviceDescriptor );
        Service.Builder builder = service.isPresent() ? Service.newBuilder( service.get() ) :
                Service.newBuilder( serviceDetails, Order.CONCURRENT );
        builder.connection( connection );
        storageInstance.putService( builder.build() );
    }
}
