/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.mbean;

import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.CasualConnectionFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.resource.AdministeredObjectDefinition;
import javax.resource.spi.AdministeredObject;
import java.io.Serializable;

/**
 * CasualMBeanImpl
 *
 * @version $Revision: $
 */
@AdministeredObject(adminObjectInterfaces = {CasualMBean.class})
@AdministeredObjectDefinition(interfaceName = "se.laz.casual.jca.mbean.CasualMBean",
        resourceAdapter = "se.laz.casual.jca.CasualResourceAdapter",
        description = "Config for Casual Resource Adapter",
        className = "se.laz.casual.jca.mbean.CasualMBeanImpl",
        name = "eis.casual-jca-0.0.1_RABean")
public class CasualMBeanImpl implements CasualMBean, Serializable
{
    private static final long serialVersionUID = 1L;
    private static final String JNDI_NAME = "java:/eis/Casual";
    private MBeanServer mbeanServer;
    private String objectName;
    private ObjectName on;
    private boolean registered;

    /**
     * Set the MBean server
     *
     * @param v The value
     */
    public void setMBeanServer(MBeanServer v)
    {
        mbeanServer = v;
    }

    /**
     * Start
     *
     * @throws Throwable Thrown in case of an error
     */
    public void start() throws Throwable
    {
        if (mbeanServer == null)
        {
            throw new IllegalArgumentException("MBeanServer is null");
        }
        on = new ObjectName(mbeanServer.getDefaultDomain() + objectName);
        mbeanServer.registerMBean(this, on);
        registered = true;
    }

    /**
     * Stop
     *
     * @throws Throwable Thrown in case of an error
     */
    public void stop() throws Throwable
    {
        if (registered)
        {
            mbeanServer.unregisterMBean(on);
        }
    }

    /**
     * GetConnection
     *
     * @return DefaultNetworkConnection
     */
    private CasualConnection getConnection() throws Exception
    {
        InitialContext context = new InitialContext();
        CasualConnectionFactory factory = (CasualConnectionFactory) context.lookup(JNDI_NAME);
        CasualConnection conn = factory.getConnection();
        if (conn == null)
        {
            throw new RuntimeException("No connection");
        }
        return conn;
    }

}
