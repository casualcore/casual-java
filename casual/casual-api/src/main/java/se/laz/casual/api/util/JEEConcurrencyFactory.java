/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.util;

import se.laz.casual.config.ConfigurationService;
import se.laz.casual.config.Outbound;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;

public class JEEConcurrencyFactory
{
    private static final Logger LOG = Logger.getLogger(JEEConcurrencyFactory.class.getName());
    // see: https://docs.jboss.org/author/display/WFLY/EE%20Subsystem%20Configuration.html
    private static final String DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME_JBOSS_ALTERNATIVE = "java:jboss/ee/concurrency/scheduler/default";
    private static final String DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME_JBOSS_ALTERNATIVE = "java:jboss/ee/concurrency/executor/default";
    private static final int SCHEDULED_THREAD_POOL_EXECUTOR_SIZE = 1;
    private static ScheduledThreadPoolExecutor fallBackScheduledExecutorService;
    private static ExecutorService fallBackExecutorService;

    private JEEConcurrencyFactory()
    {}

    public static ExecutorService getManagedExecutorService()
    {
        Outbound outbound = ConfigurationService.getInstance().getConfiguration().getOutbound();
        if(outbound.getUnmanaged())
        {
            return null;
        }
        String name = outbound.getManagedExecutorServiceName();
        try
        {
            LOG.info(() -> "using ManagedExecutorService: " + name);
            return InitialContext.doLookup(name);
        }
        catch (NamingException e)
        {
            try
            {
                LOG.warning(() -> "failed using ManagedExecutorService: " + name + " will try with: " + DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME_JBOSS_ALTERNATIVE);
                return InitialContext.doLookup(DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME_JBOSS_ALTERNATIVE);
            }
            catch (NamingException ee)
            {
                LOG.warning(() -> "failed using ManagedExecutorService: " + name + " falling back to non managed ExecutorService");
                return getFallBackExecutorService();
            }
        }
    }

    public static ScheduledExecutorService getManagedScheduledExecutorService()
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
                // On wls there is no alternative thus we end up here
                LOG.warning(() -> "failed using ManagedScheduledExecutorService: " + name + " falling back to non managed ScheduledThreadPoolExecutor");
                return getFallBackScheduledExecutorService();
            }
        }
    }

    private static ExecutorService getFallBackExecutorService()
    {
        if(null == fallBackExecutorService)
        {
            fallBackExecutorService = Executors.newWorkStealingPool();
        }
        return fallBackExecutorService;
    }

    private static synchronized ScheduledExecutorService getFallBackScheduledExecutorService()
    {
        if(null == fallBackScheduledExecutorService)
        {
            fallBackScheduledExecutorService = new ScheduledThreadPoolExecutor(SCHEDULED_THREAD_POOL_EXECUTOR_SIZE);
        }
        return fallBackScheduledExecutorService;
    }
}
