/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller.jmx;

import se.laz.casual.api.CasualRuntimeException;
import se.laz.casual.connection.caller.Cache;
import se.laz.casual.connection.caller.ConnectionFactoryEntryStore;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

@Startup
@Singleton
public class JMXStartup
{
    private static final Logger LOG = Logger.getLogger(JMXStartup.class.getName());
    private static final String NAME = "se.laz.casual.caller:type=CasualCallerControl";

    @Inject
    Cache cache;

    @Inject
    ConnectionFactoryEntryStore connectionFactoryEntryStore;

    @PostConstruct
    void initJmx()
    {
        LOG.finest("JMXStartup::begin");

        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            CasualCallerControl ccc = new CasualCallerControl(cache, connectionFactoryEntryStore);

            ObjectName objectName = new ObjectName(NAME);

            unregister(mBeanServer, objectName);

            mBeanServer.registerMBean(ccc, objectName);
        }
        catch (MalformedObjectNameException | InstanceAlreadyExistsException | NotCompliantMBeanException | MBeanRegistrationException e)
        {
            throw new CasualRuntimeException(e);
        }

        LOG.finest("JMXStartup::end");
    }

    @PreDestroy
    void tearDownJmx()
    {
        LOG.finest("JMXTeardown::begin");

        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

            ObjectName objectName = new ObjectName(NAME);

            unregister(mBeanServer, objectName);
        }
        catch (MalformedObjectNameException | MBeanRegistrationException e)
        {
            throw new CasualRuntimeException(e);
        }

        LOG.finest("JMXTeardown::end");
    }

    private void unregister(MBeanServer server, ObjectName name) throws MBeanRegistrationException
    {
        try
        {
            server.unregisterMBean(name);
        }
        catch (InstanceNotFoundException e)
        {
            // Don't care.
        }
    }
}
