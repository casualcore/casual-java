/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.discovery.DiscoveryReturn;
import se.laz.casual.jca.CasualConnection;

import javax.inject.Inject;
import javax.resource.ResourceException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class ConnectionValidator
{
    private static final Logger LOG = Logger.getLogger(ConnectionValidator.class.getName());
    Cache cache;

    // WLS - no arg constructor
    public ConnectionValidator()
    {}

    @Inject
    public ConnectionValidator(Cache cache)
    {
        this.cache = cache;
    }

    public void validate(final ConnectionFactoryEntry connectionFactoryEntry)
    {
        boolean invalidBeforeValidation = !connectionFactoryEntry.isValid();
        connectionFactoryEntry.validate();
        if(connectionReestablished(invalidBeforeValidation, connectionFactoryEntry.isValid()))
        {
            Map<CacheType, List<String>> cachedItems = cache.getAll();
            cache.purge(connectionFactoryEntry);
            Optional<DiscoveryReturn> maybeDiscoveryReturn = issueDiscovery(cachedItems, connectionFactoryEntry);
            maybeDiscoveryReturn.ifPresent(discoveryReturn ->  cache.repopulate(discoveryReturn, connectionFactoryEntry));
        }
    }

    private boolean connectionReestablished(boolean invalidBeforeRevalidation, boolean valid)
    {
        return invalidBeforeRevalidation && valid;
    }

    private Optional<DiscoveryReturn> issueDiscovery(Map<CacheType, List<String>> cachedItems, ConnectionFactoryEntry connectionFactoryEntry)
    {
        try(CasualConnection connection = connectionFactoryEntry.getConnectionFactory().getConnection())
        {
            LOG.finest(() -> "domain discovery for all known services/queues will be issued for " + connectionFactoryEntry );
            LOG.finest(() -> "all known services/queues being, services: " + cachedItems.get(CacheType.SERVICE) + " queues: " + cachedItems.get(CacheType.QUEUE));
            DiscoveryReturn discoveryReturn = connection.discover(UUID.randomUUID(),
                    cachedItems.get(CacheType.SERVICE),
                    cachedItems.get(CacheType.QUEUE));
            LOG.finest(() -> "discovery returned: " + discoveryReturn);
            return Optional.of(discoveryReturn);
        }
        catch (ResourceException e)
        {
            connectionFactoryEntry.invalidate();
            LOG.warning(() -> "failed domain discovery for: " + connectionFactoryEntry + " -> " + e);
            LOG.warning(() -> "services: " + cachedItems.get(CacheType.SERVICE) + " queues: " + cachedItems.get(CacheType.QUEUE));
        }
        return Optional.empty();
    }

}
