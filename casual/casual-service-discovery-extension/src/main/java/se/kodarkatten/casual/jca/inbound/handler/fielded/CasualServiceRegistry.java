package se.kodarkatten.casual.jca.inbound.handler.fielded;

import se.kodarkatten.casual.api.services.CasualService;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by jone on 2017-02-27.
 */
public final class CasualServiceRegistry
{
    private static final Logger LOG = Logger.getLogger(CasualServiceRegistry.class.getName());

    private final Map<String,CasualServiceEntry> services;

    private static final CasualServiceRegistry instance = new CasualServiceRegistry();

    private CasualServiceRegistry()
    {
        services = new ConcurrentHashMap<>();
    }

    public static final CasualServiceRegistry getInstance()
    {
        return instance;
    }

    public void register(CasualService service, Method serviceMethod, Class<?> serviceClass)
    {
        CasualServiceEntry registryEntry = new CasualServiceEntry(service, serviceMethod, serviceClass);

        validateServiceEntry(registryEntry);

        services.put( service.name(), registryEntry );
    }

    public boolean hasEntry( String name )
    {
        return services.containsKey( name );
    }

    public CasualServiceEntry getEntry( String name )
    {
        return services.get( name );
    }

    public void deregister( CasualService service )
    {
        //TODO: listen to application destruction to remove from the map.
    }

    public int size()
    {
        return services.size();
    }

    private void validateServiceEntry(CasualServiceEntry registryEntry)
    {
        //TODO: what are the validation rules?
    }



}
