package se.laz.casual.jca.jmx;

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
      Map<String, NetworkConnectionPool> pools = NetworkPoolHandler.getInstance().getPools();
      return pools.keySet().stream()
                  .map(key -> key + "=" + pools.get(key))
                  .collect(Collectors.toList());
   }

   @Override
   public String getNetworkPoolByName(String name)
   {
      NetworkConnectionPool pool = NetworkPoolHandler.getInstance().getPool(name);
      return null == pool ?  "no network connection pool with name: " + name : pool.toString();
   }

}
