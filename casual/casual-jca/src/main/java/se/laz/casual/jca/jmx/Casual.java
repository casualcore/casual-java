package se.laz.casual.jca.jmx;

import se.laz.casual.jca.Address;
import se.laz.casual.jca.DomainHandler;
import se.laz.casual.jca.DomainId;
import se.laz.casual.jca.DomainIdReferenceCounted;
import se.laz.casual.jca.pool.NetworkConnectionPool;
import se.laz.casual.jca.pool.NetworkPoolHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Casual implements CasualMBean
{

   @Override
   public List<String> networkPools()
   {
      Map<Address, NetworkConnectionPool> pools = NetworkPoolHandler.getInstance().getPools();
      return pools.keySet().stream()
                  .map(key -> key + "=" + pools.get(key))
                  .collect(Collectors.toList());

   }

   @Override
   public List<String> domainIds()
   {
      Map<Address, List<DomainIdReferenceCounted>> domainIds = DomainHandler.getInstance().getDomainIds();
      return domainIds.keySet().stream()
                      .map(key -> key + "=" + domainIds.get(key))
                      .collect(Collectors.toList());
   }

   @Override
   public String getNetworkPoolForAddress(String host, String port)
   {
      Address address = Address.of(host, port);
      NetworkConnectionPool pool = NetworkPoolHandler.getInstance().getPool(address);
      return null == pool ?  "no network connection pool for: " + address : pool.toString();
   }

   @Override
   public List<String> getDomainIdsForAddress(String host, String port)
   {
      Address address = Address.of(host, port);
      List<DomainId> domainIds = DomainHandler.getInstance().getDomainIds(address);
      return domainIds
              .stream()
              .map(DomainId::toString)
              .collect(Collectors.toList());
   }

}
