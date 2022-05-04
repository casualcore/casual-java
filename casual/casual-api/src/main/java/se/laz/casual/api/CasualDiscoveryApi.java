package se.laz.casual.api;

import se.laz.casual.api.discovery.DiscoveryReturn;

import java.util.List;
import java.util.UUID;

public interface CasualDiscoveryApi
{
    DiscoveryReturn discover(UUID corrid, List<String> serviceNames, List<String> queueNames);
}
