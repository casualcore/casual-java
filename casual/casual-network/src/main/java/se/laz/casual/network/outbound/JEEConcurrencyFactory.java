/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import se.laz.casual.config.ConfigurationService;
import se.laz.casual.config.Outbound;
import se.laz.casual.jca.CasualResourceAdapterException;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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

    private JEEConcurrencyFactory()
    {}

    /**
     * Gets default managed executor service - on jboss
     * On wls it will throw CasualResourceAdapterException unless a direct JNDI-name is configured
     * @return an ExecutorService
     */
    public static ManagedExecutorService getManagedExecutorService()
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
                LOG.warning(() -> "failed using ManagedExecutorService: " + name + " will try with: " + DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME_JBOSS_DIRECT);
                return InitialContext.doLookup(DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME_JBOSS_DIRECT);
            }
            catch (NamingException ee)
            {
                throw new CasualResourceAdapterException("failed lookup for: " + name + "\n outbound will not function!", e);
            }
        }
    }

}
