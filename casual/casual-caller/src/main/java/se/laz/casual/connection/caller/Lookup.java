/*
 * Copyright (c) 2017 - 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.CasualConnectionFactory;

import javax.resource.ResourceException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Lookup
{
    public  List<ConnectionFactoryEntry> find(QueueInfo qinfo, List<ConnectionFactoryEntry> cacheEntries)
    {
        return find(cacheEntries, con -> con.queueExists(qinfo));
    }

    public  List<ConnectionFactoryEntry> find(String serviceName, List<ConnectionFactoryEntry> cacheEntries)
    {
        return find(cacheEntries, con -> con.serviceExists(serviceName));
    }

    private List<ConnectionFactoryEntry> find(List<ConnectionFactoryEntry> cacheEntries, Predicate<CasualConnection> predicate)
    {
        List<ConnectionFactoryEntry> foundEntries = new ArrayList<>();
        for(ConnectionFactoryEntry entry : cacheEntries)
        {
            CasualConnectionFactory connectionFactory = entry.getConnectionFactory();
            try(CasualConnection con = connectionFactory.getConnection())
            {
                if(predicate.test(con))
                {
                    foundEntries.add(entry);
                }
            }
            catch (ResourceException e)
            {
                throw new CasualResourceException(e);
            }
        }
        return foundEntries;
    }

}
