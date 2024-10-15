/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.jmx;

import se.laz.casual.jca.pool.NetworkConnectionPool;
import se.laz.casual.jca.pool.NetworkPoolHandler;

import java.util.List;
import java.util.Map;

public class Casual implements CasualMBean
{

   @Override
   public List<String> networkPools()
   {
      Map<String, NetworkConnectionPool> pools = NetworkPoolHandler.getInstance().getPools();
      return pools.keySet().stream()
                  .map(key -> key + "=" + pools.get(key))
                  .toList();
   }

   @Override
   public String getNetworkPoolByName(String name)
   {
      NetworkConnectionPool pool = NetworkPoolHandler.getInstance().getPool(name);
      return null == pool ?  "no network connection pool with name: " + name : pool.toString();
   }

}
