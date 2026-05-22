package se.laz.casual.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Static instance that contains information about inbound Casual Services.
 * Used primarily by casual-java cli.
 */
public final class CasualInfo
{
    private static CasualInfo instance;
    private static final Map<String, Service> inboundServices = new HashMap<>();

    private CasualInfo() {}

    public static CasualInfo getInstance() {
        if(instance == null) {
            instance = new CasualInfo();
        }
        return instance;
    }

    public void addInboundService(Service service) {
        inboundServices.put(service.getName(), service);
    }

    public List<Service> getServices() {
        return new ArrayList<>(inboundServices.values());
    }

    public Optional<Service> getInboundService(String name)
    {
        return Optional.ofNullable(inboundServices.get(name));
    }
}
