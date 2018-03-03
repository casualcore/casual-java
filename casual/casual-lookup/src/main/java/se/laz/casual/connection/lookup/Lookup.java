/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.lookup;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.CasualConnectionFactory;

import javax.enterprise.context.Dependent;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import java.util.List;
import java.util.Optional;

@Dependent
public class Lookup
{
    public Optional<String> findJNDIName(QueueInfo qinfo, InitialContext ctx, List<String> casualJNDINames)
    {
        for(String jndiname : casualJNDINames)
        {
            try(CasualConnection con = ((CasualConnectionFactory) ctx.lookup(jndiname)).getConnection())
            {
                if(con.queueExists(qinfo))
                {
                    return Optional.of(jndiname);
                }
            }
            catch (NamingException | ResourceException e)
            {
                throw new CasualLookupException(e);
            }
        }
        return Optional.empty();
    }

    public Optional<String> findJNDIName(ServiceInfo serviceInfo, InitialContext ctx, List<String> casualJNDINames)
    {
        for(String jndiname : casualJNDINames)
        {
            try(CasualConnection con = ((CasualConnectionFactory) ctx.lookup(jndiname)).getConnection())
            {
                if(con.serviceExists(serviceInfo.getServiceName()))
                {
                    return Optional.of(jndiname);
                }
            }
            catch (NamingException | ResourceException e)
            {
                throw new CasualLookupException(e);
            }
        }
        return Optional.empty();
    }
}
