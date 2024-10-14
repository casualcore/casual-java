/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.jca.CasualResourceAdapterException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

public class JEEConcurrencyFactory
{
    private static final Logger LOG = Logger.getLogger(JEEConcurrencyFactory.class.getName());
    // An indirect, via java:comp, JNDI lookup can only be done from an application within a J2EE container (Web module, EJB module, or application client module).
    // Hence, we need to issue direct JNDI lookup since that is not true for us - yet, due to the packaging of casual-jca

    // JBOSS direct JNDI names for EE Concurrency utils
    // see: https://docs.jboss.org/author/display/WFLY/EE%20Subsystem%20Configuration.html
    // For wls:
    // * we have not found what the direct names are so if you run on wls it will throw unless direct a direct JNDI-name is configured
    // * this goes for conversation, it needs an ExecutorService, and Outbound - if not set to run unmanaged
    private static final String DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME_JBOSS_DIRECT = "java:jboss/ee/concurrency/executor/default";

    public static final String DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME_JBOSS_DIRECT = "java:jboss/ee/concurrency/scheduler/default";
    public static final String DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME_INDIRECT = "java:comp/DefaultManagedScheduledExecutorService";

    // For configuring the concurrency of the normal java.util.concurrency-based ScheduledExecutorService which is
    // only used whenever the direct jboss-variant or the jee-spec indirect jndi name lookups fail for a scheduled
    // executor service.
    public static final String CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE_ENV_NAME = "CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE";
    public static final int DEFAULT_CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE = 10;

    /** Fallback ScheduledExecutorService that is used when nothing is found through jndi */
    private static ScheduledExecutorService sharedScheduledExecutor;

    private JEEConcurrencyFactory()
    {}

    /**
     * Gets default managed executor service - on jboss
     * On wls it will throw CasualResourceAdapterException unless a direct JNDI-name is configured
     * @return an ExecutorService
     */
    public static ManagedExecutorService getManagedExecutorService()
    {
        boolean unmanaged = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_OUTBOUND_UNMANAGED );
        if(unmanaged)
        {
            return null;
        }
        String name = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME );
        try
        {
            LOG.info(() -> "using ManagedExecutorService: " + name);
            return InitialContext.doLookup(name);
        }
        catch (NamingException e)
        {
            try
            {
                LOG.warning(() -> "failed using ManagedExecutorService: " + name + " will try with: " + DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME_JBOSS_DIRECT);
                return InitialContext.doLookup(DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME_JBOSS_DIRECT);
            }
            catch (NamingException ee)
            {
                throw new CasualResourceAdapterException("failed lookup for: " + name + "\n outbound will not function!", e);
            }
        }
    }

    /**
     * Gets the default ManagedScheduledExecutorService on jboss. On some non-standard implementation
     * it will fetch the default through java:comp/DefaultManagedScheduledExecutorService
     * which normally shouldn't be accessible in this context.
     * <p>
     * If both of those fail a ScheduledExecutorService instance will be created
     * through the standard java.util.concurrent.Executor which will share the
     * same ScheduledExecutorService among all users.
     *
     * @return A ScheduledExecutorService instance.
     */
    public static ScheduledExecutorService getManagedScheduledExecutorService()
    {
        try
        {
            // First try jboss variant
            return InitialContext.doLookup(DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME_JBOSS_DIRECT);
        }
        catch (NamingException e)
        {
            LOG.info("Failed lookup for " + DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME_JBOSS_DIRECT + ", will retry indirect jndi name (may exist in this context on some non-standard application servers)");
            try
            {
                // Second try non-standard use of indirect jndi name defined in JSR-236
                return InitialContext.doLookup(DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME_INDIRECT);
            }
            catch (NamingException ex)
            {
                LOG.info("Failed lookup for " + DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME_INDIRECT + ", will use scheduled executor from java.util.concurrent.Executors");
                // If all else fails, use java.util.concurrent variant as scheduler
                return getSharedJavaUtilScheduledExecutor();
            }
        }
    }

    static synchronized ScheduledExecutorService getSharedJavaUtilScheduledExecutor()
    {
        if (null == sharedScheduledExecutor)
        {
            int schedulerPoolSize = DEFAULT_CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE;

            final String poolSizeEnvValue = System.getenv(CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE_ENV_NAME);
            try
            {
                schedulerPoolSize = Integer.parseInt(poolSizeEnvValue);
            }
            catch (NumberFormatException e)
            {
                LOG.info("Failed to read env '" + CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE_ENV_NAME
                        + "' as int, value was '" + poolSizeEnvValue
                        + "'. Will use default pool size=" + schedulerPoolSize);
            }

            sharedScheduledExecutor = Executors.newScheduledThreadPool(schedulerPoolSize);
        }

        return sharedScheduledExecutor;
    }

    /**
     * For testing purposes only
     */
    static void resetScheduler()
    {
        sharedScheduledExecutor = null;
    }
}
