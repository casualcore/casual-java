/*
 * Copyright (c) 2017 - 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.connection.caller.util.ConnectionFactoryFinder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class ConnectionFactoryProvider
{
    private static final Logger LOG = Logger.getLogger(ConnectionFactoryProvider.class.getName());
    private static final String DEFAULT_JNDI_ROOT_ENV_NAME = "CASUAL_CALLER_CONNECTION_FACTORY_JNDI_SEARCH_ROOT";
    private static final String DEFAULT_JNDI_ROOT_ENV_VALUE = "eis";
    private final StaleConnectionFactoryHandler staleConnectionFactoryHandler;
    private final ConnectionFactoryFinder connectionFactoryFinder;
    private List<ConnectionFactoryEntry> connectionFactories;

    public ConnectionFactoryProvider()
    {
        // public NOP-constructor needed for wls-only
        staleConnectionFactoryHandler = null;
        connectionFactoryFinder = null;
    }

    @Inject
    public ConnectionFactoryProvider(StaleConnectionFactoryHandler staleConnectionFactoryHandler, ConnectionFactoryFinder connectionFactoryFinder)
    {
        this.staleConnectionFactoryHandler = staleConnectionFactoryHandler;
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
        connectionFactories = staleConnectionFactoryHandler.revalidateConnectionFactories(connectionFactories);
        return Collections.unmodifiableList(connectionFactories);
    }

    @PostConstruct
    public synchronized void initialize()
    {
        connectionFactories = connectionFactoryFinder.findConnectionFactory(getJndiRoot());
    }

    private String getJndiRoot()
    {
        return Optional.ofNullable(System.getenv(DEFAULT_JNDI_ROOT_ENV_NAME)).orElse(DEFAULT_JNDI_ROOT_ENV_VALUE);
    }
}
