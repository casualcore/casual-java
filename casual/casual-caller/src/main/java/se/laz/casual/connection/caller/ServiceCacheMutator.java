package se.laz.casual.connection.caller;

@FunctionalInterface
public interface ServiceCacheMutator
{
    void removeFromServiceCache(String serviceName);
}
