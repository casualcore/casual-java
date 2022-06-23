/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network;

import se.laz.casual.config.ConfigurationService;
import se.laz.casual.config.Outbound;
import se.laz.casual.jca.CasualResourceAdapterException;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Logger;

public class JEEConcurrencyFactory
{
    private static final Logger LOG = Logger.getLogger(JEEConcurrencyFactory.class.getName());
    private static final String DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME_JBOSS_ALTERNATIVE = "java:jboss/ee/concurrency/scheduler/default";
    private JEEConcurrencyFactory()
    {}

    public static ManagedExecutorService getManagedExecutorService()
    {
        Outbound outbound = ConfigurationService.getInstance().getConfiguration().getOutbound();
        String name = outbound.getManagedExecutorServiceName();
        try
        {
            LOG.info(() -> "using ManagedExecutorService: " + name);
            return InitialContext.doLookup(name);
        }
        catch (NamingException e)
        {
            throw new CasualResourceAdapterException("failed lookup for: " + name + "\n outbound will not function!", e);
        }
    }

    public static ManagedScheduledExecutorService getManagedScheduledExecutorService()
    {
        Outbound outbound = ConfigurationService.getInstance().getConfiguration().getOutbound();
        String name = outbound.getManagedScheduledExecutorServiceName();
        try
        {
            LOG.info(() -> "using ManagedScheduledExecutorService: " + name);
            return InitialContext.doLookup(name);
        }
        catch (NamingException e)
        {
            try
            {
                // according to http://docs.wildfly.org/26.1/Developer_Guide.html#managed-scheduled-executor-service
                // java:comp/DefaultManagedScheduledExecutorService should always exist but seems it does not
                // TODO: figure out why it is missing when it should clearly be there!
                LOG.warning(() -> "failed using ManagedScheduledExecutorService: " + name + " will try with: " + DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME_JBOSS_ALTERNATIVE);
                return InitialContext.doLookup(DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME_JBOSS_ALTERNATIVE);
            }
            catch(NamingException ee)
            {
                throw new CasualResourceAdapterException("failed lookup for: " + name + "\n outbound will not function!", ee);
            }
        }
    }
}
