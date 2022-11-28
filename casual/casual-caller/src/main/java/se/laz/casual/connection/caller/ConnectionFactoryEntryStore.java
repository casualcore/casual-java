/*
 * Copyright (c) 2017 - 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.connection.caller.config.ConfigurationService;
import se.laz.casual.connection.caller.util.ConnectionFactoryFinder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class ConnectionFactoryEntryStore
{
    private static final Logger LOG = Logger.getLogger(ConnectionFactoryEntryStore.class.getName());
    private final ConnectionFactoryFinder connectionFactoryFinder;
    private List<ConnectionFactoryEntry> connectionFactories;

    public ConnectionFactoryEntryStore()
    {
        // public NOP-constructor needed for wls-only
        connectionFactoryFinder = null;
    }

    @Inject
    public ConnectionFactoryEntryStore(ConnectionFactoryFinder connectionFactoryFinder)
    {
        this.connectionFactoryFinder = connectionFactoryFinder;
    }

    public List<ConnectionFactoryEntry> get()
    {
        if(connectionFactories.isEmpty())
        {
            initialize();
            if(connectionFactories.isEmpty())
            {
                LOG.warning(() -> "could not find any connection factories, casual-caller will not work. Will retry on next access.\n Either your configuration is wrong or the entries do not yet exist in the JNDI-tree just yet.");
            }
        }
        return Collections.unmodifiableList(connectionFactories);
    }

    @PostConstruct
    public synchronized void initialize()
    {
        connectionFactories = connectionFactoryFinder.findConnectionFactory(getJndiRoot());
    }

    private String getJndiRoot()
    {
        return ConfigurationService.getInstance().getConfiguration().getJndiSearchRoot();
    }
}
