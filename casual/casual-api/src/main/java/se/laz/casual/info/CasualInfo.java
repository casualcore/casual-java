package se.laz.casual.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static instance that contains information about inbound Casual Services.
 * Used primarily by casual-java cli.
 */
public final class CasualInfo
{
    private static final CasualInfo instance = new CasualInfo();
    private final Map<String, Service> inboundServices;

    private CasualInfo() {
        this.inboundServices = new ConcurrentHashMap<>();
    }

    public static void addInboundService(Service service) {
        Objects.requireNonNull(service, "service must not be null");
        instance.inboundServices.put(service.getName(), service);
    }

    public static List<Service> getServices() {
        return new ArrayList<>(instance.inboundServices.values());
    }

    public static Optional<Service> getInboundService(String name)
    {
        Objects.requireNonNull(name, "name must not be null");
        return Optional.ofNullable(instance.inboundServices.get(name));
    }
}
