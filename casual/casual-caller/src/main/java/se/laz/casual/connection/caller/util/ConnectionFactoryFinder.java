/*
 * Copyright (c) 2021. The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.util;

import se.laz.casual.connection.caller.ConnectionFactoryProducer;
import se.laz.casual.connection.caller.ConnectionFactoryEntry;
import se.laz.casual.jca.CasualConnectionFactory;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class ConnectionFactoryFinder
{
    private static final Logger log = Logger.getLogger(ConnectionFactoryFinder.class.getName());
    public ConnectionFactoryFinder()
    {
        // public NOP-constructor needed for wls-only
    }

    public static ConnectionFactoryFinder of()
    {
        return new ConnectionFactoryFinder();
    }

    public List<ConnectionFactoryEntry> findConnectionFactory(String root)
    {
        try
        {
            InitialContext ctx = new InitialContext();
            return findConnectionFactory(root, ctx);
        }
        catch (NamingException e)
        {
            log.warning(() -> "CasualConnectionFactory lookup failed, using CasualCaller will not work\n\n" + e);
        }
        return Collections.<ConnectionFactoryEntry>emptyList();
    }

    public List<ConnectionFactoryEntry> findConnectionFactory(String root, InitialContext context)
    {
        try
        {
            List<ConnectionFactoryEntry> foundEntries = new ArrayList<>();
            NamingEnumeration<NameClassPair> entries = context.list(root);
            while(entries.hasMoreElements())
            {
                NameClassPair entry = entries.nextElement();
                String jndiName = entry.isRelative() ? root + "/" + entry.getName() : entry.getName();
                Object instance = context.lookup(jndiName);
                if(instance instanceof CasualConnectionFactory)
                {
                    foundEntries.add(ConnectionFactoryEntry.of(ConnectionFactoryProducer.of(jndiName)));
                    log.info(() -> "found casual connection factory with JNDI-name: " + jndiName);
                }
            }
            return foundEntries;
        }
        catch (NamingException e)
        {
            log.warning(() -> "CasualConnectionFactory lookup failed, using CasualCaller will not work\n\n" + e);
        }
        return Collections.<ConnectionFactoryEntry>emptyList();
    }
}
