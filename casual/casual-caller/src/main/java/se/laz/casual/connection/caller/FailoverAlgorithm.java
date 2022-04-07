/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.jca.CasualConnection;
import se.laz.casual.network.connection.CasualConnectionException;

import javax.resource.ResourceException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FailoverAlgorithm<T>
{
    private static final Logger LOG = Logger.getLogger(FailoverAlgorithm.class.getName());

    public T callWithFailover(
            String serviceName,
            ConnectionFactoryLookup lookup,
            FunctionThrowsResourceException<CasualConnection, T> doCall,
            FunctionNoArg<T> doTpenoent)
    {
        Exception thrownException = null;

        List<ConnectionFactoryEntry> prioritySortedFactories = lookup.get(serviceName);

        // Service not found
        if (prioritySortedFactories.isEmpty())
        {
            LOG.warning("No priority or service targets at all");
            return doTpenoent.apply();
        }

        List<ConnectionFactoryEntry> validEntries = prioritySortedFactories.stream().filter(ConnectionFactoryEntry::isValid).collect(Collectors.toList());

        LOG.finest(() -> "Running call to '" + serviceName + "' with " + validEntries.size() + " of " + prioritySortedFactories.size() + " possible connection factories");

        // No valid casual server found (revalidation is on a timer in ConnectionFactoryEntryValidationTimer)
        if (validEntries.isEmpty())
        {
            throw new CasualResourceException("Received a set of ConnectionFactoryEntries, but not one was valid");
        }

        for (ConnectionFactoryEntry connectionFactoryEntry : validEntries)
        {
            try (CasualConnection con = connectionFactoryEntry.getConnectionFactory().getConnection())
            {
                return doCall.apply(con);
            }
            catch (CasualConnectionException e)
            {
                //This error branch will most likely happen if there are connection errors during a service call
                connectionFactoryEntry.invalidate();

                // These exceptions are rollback-only, do not attempt any retries.
                throw new CasualResourceException("Call failed during execution to service=" + serviceName + " on connection=" + connectionFactoryEntry.getJndiName() + " because of a network connection error, retries not possible.", e);
            }
            catch (ResourceException e)
            {
                // This error branch will most likely happen on failure to establish connection with a casual backend
                connectionFactoryEntry.invalidate();

                // Do retries on ResourceExceptions. Save the thrown exception and return to the loop
                // If there are more entries to try that will be done, or the flow will exit and this
                // exception will be thrown wrapped at the end of the method.
                thrownException = e;
            }
        }
        throw new CasualResourceException("Call failed to all " + validEntries.size() + " available casual connections connections.", thrownException);
    }

    public interface FunctionNoArg<R>
    {
        R apply();
    }

    public interface FunctionThrowsResourceException<I, R>
    {
        R apply(I input) throws ResourceException;
    }
}
