package se.laz.casual.jca.jmx;

import java.util.List;

public interface CasualMBean
{
    List<String> networkPools();
    String getNetworkPoolByName(String name);
}
