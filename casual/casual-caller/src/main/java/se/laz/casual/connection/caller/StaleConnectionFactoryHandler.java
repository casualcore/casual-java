/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

import se.laz.casual.jca.CasualConnectionFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

// connection factories can go stale, at least on WLS
// if so we need to issue a new lookup
public class StaleConnectionFactoryHandler
{
    private static final Logger LOG = Logger.getLogger(StaleConnectionFactoryHandler.class.getName());
    private Event<ConnectionFactoryEntryChangedEvent> entryChangedEvent;

    @Inject
    public StaleConnectionFactoryHandler(Event<ConnectionFactoryEntryChangedEvent> entryChangedEvent)
    {
        this.entryChangedEvent = entryChangedEvent;
    }

    public static StaleConnectionFactoryHandler of(Event<ConnectionFactoryEntryChangedEvent> entryChangedEvent)
    {
        return new StaleConnectionFactoryHandler( entryChangedEvent );
    }

    public List<ConnectionFactoryEntry> revalidateConnectionFactories(List<ConnectionFactoryEntry> connectionFactories)
    {
        try
        {
            InitialContext context = new InitialContext();
            return revalidateConnectionFactories(connectionFactories, context);
        }
        catch (NamingException e)
        {
            throw new CasualCallerException("could not create InitialContext", e);
        }
    }

    public List<ConnectionFactoryEntry> revalidateConnectionFactories(List<ConnectionFactoryEntry> connectionFactories, InitialContext context)
    {
        List<ConnectionFactoryEntry> entries = new ArrayList<>();
        entries.addAll(connectionFactories);
        entries.replaceAll(entry -> replaceStale(entry, context));
        return entries;
    }

    private ConnectionFactoryEntry replaceStale(ConnectionFactoryEntry entry, InitialContext context)
    {
        if(isStale(entry))
        {
            return recreate(entry.getJndiName(), context).orElseGet( () ->{
                LOG.warning(() -> "Failed recreating stale ConnectionFactoryEntry: " + entry + " will keep using the old one");
                return entry;
            });
        }
        return entry;
    }

    private Optional<ConnectionFactoryEntry> recreate(String jndiName, InitialContext context)
    {
        try
        {
            CasualConnectionFactory connectionFactory = (CasualConnectionFactory) context.lookup(jndiName);
            ConnectionFactoryEntry entry = ConnectionFactoryEntry.of(jndiName, connectionFactory);
            LOG.info(() -> "recreated stale ConnectionFactoryEntry: " + entry);
            entryChangedEvent.fire(new ConnectionFactoryEntryChangedEvent(jndiName, entry));
            return Optional.of(entry);
        }
        catch (NamingException e)
        {
            return Optional.empty();
        }
    }

    private static boolean isStale(ConnectionFactoryEntry entry)
    {
        entry.validate();
        return entry.isInvalid();
    }

}
