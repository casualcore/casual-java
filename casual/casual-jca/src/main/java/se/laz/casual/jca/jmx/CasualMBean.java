package se.laz.casual.jca.jmx;

import java.util.List;

public interface CasualMBean
{
   List<String> networkPools();
   List<String> domainIds();

   String getNetworkPoolForAddress(String host, String port);
   List<String> getDomainIdsForAddress(String host, String port);
}
