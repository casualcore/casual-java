package se.laz.casual.info;

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
    private final CasualInfoStorage storageInstance;

    private CasualInfo()
    {
        storageInstance = new CasualInfoStorage();
    }

    /**
     * Add service to storage
     *
     * @param service - a service
     */
    public static void addService( Service service )
    {
        Objects.requireNonNull( service, "service must not be null" );
        instance.storageInstance.putService( service );
    }

    /**
     * Set inbound service as registered and populate jndi name
     * @param serviceName   - name of service
     * @param jndiName      - Jndi name of service
     */
    public static void registerService( String serviceName, String jndiName )
    {
        // Set information about a service when it has been registered:
        List<Service> serviceList = instance.storageInstance.getService(serviceName);
        Optional<Service> storedService = serviceList.stream().filter(service ->
                service.getName().equals(serviceName) && service.getOrder().equals(Order.SEQUENTIAL)).findFirst();
        // Update service in local storage:
        storedService.ifPresent( service -> instance.storageInstance.putService(
                Service.newBuilder( service )
                        .registred( true )
                        .jndiName( jndiName )
                        .build()));
    }

    /**
     * Get all instances for a specific service name
     * @param serviceName - name of serivice
     * @return List of instances of service
     */
    public static List<Service> getService( String serviceName )
    {
        return instance.storageInstance.getService( serviceName );
    }

    /**
     * Returns all services
     * @return List of services
     */
    public static List<Service> getServices()
    {
        return instance.storageInstance.getServices();
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
    public static void storeEvent( String serviceName, char order, long start, long end )
    {
        instance.storageInstance.putEvent( serviceName, order, start, end );
    }

    /**
     * Get service statistics
     *
     * @param serviceDescriptor - composite key of service name and service order
     * @return Optional EventServiceStatistics
     */
    public static Optional<EventServiceStatistics> getServiceStatistic( ServiceDescriptor serviceDescriptor )
    {
        return instance.storageInstance.getServiceStatistic( serviceDescriptor );
    }

    static void clear()
    {
        instance.storageInstance.clear();
    }
}
