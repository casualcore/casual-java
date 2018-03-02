package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import se.kodarkatten.casual.api.service.CasualService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by jone on 2017-02-27.
 */
public final class CasualServiceRegistry
{
    private final Map<String,CasualServiceMetaData> serviceMetaData;
    private final Map<String,CasualServiceEntry> serviceEntries;

    private static final CasualServiceRegistry instance = new CasualServiceRegistry();

    private CasualServiceRegistry()
    {
        serviceMetaData = new ConcurrentHashMap<>();
        serviceEntries = new ConcurrentHashMap<>();
    }

    public static CasualServiceRegistry getInstance()
    {
        return instance;
    }

    public void register( CasualServiceMetaData metaData )
    {
        validateServiceMetaData(metaData);

        serviceMetaData.put( metaData.getServiceName(), metaData );
    }

    public void register( CasualServiceEntry entry )
    {
        serviceMetaData.get( entry.getServiceName() ).setResolvedEntry( entry );
        serviceEntries.put( entry.getServiceName(), entry );
    }

    public boolean hasServiceMetaData(String name )
    {
        return serviceMetaData.containsKey( name );
    }

    public boolean hasServiceEntry( String name )
    {
        return serviceEntries.containsKey( name );
    }

    public CasualServiceMetaData getServiceMetaData(String name )
    {
        return serviceMetaData.get( name );
    }

    public CasualServiceEntry getServiceEntry( String name )
    {
        return serviceEntries.get( name );
    }

    public void deregister( CasualService service )
    {
        //TODO: listen to application destruction to remove from the map.
    }

    public int serviceMetaDataSize()
    {
        return serviceMetaData.size();
    }
    public int serviceEntrySize()
    {
        return serviceEntries.size();
    }

    private void validateServiceMetaData(CasualServiceMetaData registryEntry)
    {
        //TODO: what are the validation rules?
    }

    public List<CasualServiceMetaData> getUnresolvedServices()
    {
        return serviceMetaData.values().stream().filter(CasualServiceMetaData::isUnresolved).collect(Collectors.toList());
    }

}
