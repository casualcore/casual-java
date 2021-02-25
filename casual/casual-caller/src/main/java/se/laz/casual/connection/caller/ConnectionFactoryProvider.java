/*
 * Copyright (c) 2017 - 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.connection.caller.util.ConnectionFactoryFinder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ConnectionFactoryProvider
{
    private static final String DEFAULT_JNDI_ROOT_ENV_NAME = "CASUAL_CALLER_CONNECTION_FACTORY_JNDI_SEARCH_ROOT";
    private static final String DEFAULT_JNDI_ROOT_ENV_VALUE = "eis";

    private List<ConnectionFactoryEntry> connectionFactories;

    public List<ConnectionFactoryEntry> get()
    {
        return Collections.unmodifiableList(connectionFactories);
    }

    @PostConstruct
    public void initialize()
    {
        connectionFactories = ConnectionFactoryFinder.findConnectionFactory(getJndiRoot());
    }

    private String getJndiRoot()
    {
        return Optional.ofNullable(System.getenv(DEFAULT_JNDI_ROOT_ENV_NAME)).orElse(DEFAULT_JNDI_ROOT_ENV_VALUE);
    }
}
