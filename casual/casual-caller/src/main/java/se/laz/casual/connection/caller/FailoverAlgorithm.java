/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.jca.CasualConnection;
import se.laz.casual.network.connection.CasualConnectionException;

import javax.resource.ResourceException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FailoverAlgorithm
{
    private static final Logger LOG = Logger.getLogger(FailoverAlgorithm.class.getName());
    private static final String ALL_FAIL_MESSAGE = "Received a set of ConnectionFactoryEntries, but not one was valid for service ";

    public ServiceReturn<CasualBuffer> tpcallWithFailover(
            String serviceName,
            ConnectionFactoryLookup lookup,
            FunctionThrowsResourceException<CasualConnection, ServiceReturn<CasualBuffer>> doCall,
            FunctionNoArg<ServiceReturn<CasualBuffer>> doTpenoent)
    {
        List<ConnectionFactoryEntry> validEntries = getFoundAndValidEntries(lookup, serviceName);

        // No valid casual server found (revalidation is on a timer in ConnectionFactoryEntryValidationTimer)
        if (validEntries.isEmpty())
        {
            LOG.warning(() -> ALL_FAIL_MESSAGE + serviceName);
            return doTpenoent.apply();
        }

        ServiceReturn<CasualBuffer> result = issueCall(serviceName, validEntries, doCall);
        if (result.getErrorState() == ErrorState.TPENOENT)
        {
            // using a known cached service entry results in TPENOENT
            // clear the service from the cache ( for all pools), get potentially new entries
            // issue call again if possible
            lookup.removeFromServiceCache(serviceName);
            validEntries = getFoundAndValidEntries(lookup, serviceName);
            // No valid casual server found (revalidation is on a timer in ConnectionFactoryEntryValidationTimer)
            if (validEntries.isEmpty())
            {
                LOG.warning(() -> ALL_FAIL_MESSAGE + serviceName);
                return doTpenoent.apply();
            }
            result = issueCall(serviceName, validEntries, doCall);
        }
        return result;
    }

    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacallWithFailover(
            String serviceName,
            ConnectionFactoryLookup lookup,
            FunctionThrowsResourceException<CasualConnection, CompletableFuture<ServiceReturn<CasualBuffer>>> doCall,
            FunctionNoArg<CompletableFuture<ServiceReturn<CasualBuffer>>> doTpenoent)
    {
        List<ConnectionFactoryEntry> validEntries = getFoundAndValidEntries(lookup, serviceName);

        // No valid casual server found (revalidation is on a timer in ConnectionFactoryEntryValidationTimer)
        if (validEntries.isEmpty())
        {
            LOG.warning(() -> ALL_FAIL_MESSAGE + serviceName);
            return doTpenoent.apply();
        }
        return issueCall(serviceName, validEntries, doCall);
    }

    private List<ConnectionFactoryEntry> getFoundAndValidEntries(ConnectionFactoryLookup lookup, String serviceName)
    {
        // This is always through the cache, either it was already there or a lookup was issued and then stored
        List<ConnectionFactoryEntry> prioritySortedFactories = lookup.get(serviceName);
        List<ConnectionFactoryEntry> validEntries = prioritySortedFactories.stream().filter(ConnectionFactoryEntry::isValid).collect(Collectors.toList());
        LOG.finest(() -> "Entries found for '" + serviceName + "' with " + validEntries.size() + " of " + prioritySortedFactories.size() + " possible connection factories");
        return validEntries;
    }

    private <T> T issueCall(String serviceName, List<ConnectionFactoryEntry> validEntries, FunctionThrowsResourceException<CasualConnection, T> doCall)
    {
        Exception thrownException = null;

        // Sticky transaction handling
        try
        {
            Optional<T> stickyMaybe = handleTransactionSticky(serviceName, validEntries, doCall);

            if (stickyMaybe.isPresent())
            {
                return stickyMaybe.get();
            }
        }
        catch (ResourceException e)
        {
            thrownException = e;
        }

        // Normal flow
        for (ConnectionFactoryEntry connectionFactoryEntry : validEntries)
        {
            try (CasualConnection con = connectionFactoryEntry.getConnectionFactory().getConnection())
            {
                T returnValue = doCall.apply(con);
                TransactionPoolMapper.getInstance().setPoolNameForCurrentTransaction(connectionFactoryEntry.getJndiName());
                return returnValue;
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
        throw new CasualResourceException("Call failed to all " + validEntries.size() + " available casual connections.", thrownException);
    }

    /**
     * Try to use a stickied pool if pool stickiness is configured and any stickied pool is available and serves the requested service
     *
     * @param serviceName Service to call
     * @param factories   Currently available factories for service to call
     * @param doCall      Provided service call procedure
     * @return Optional ServiceReturn. An empty result could indicate that stickiness isn't enabled, sticky isn't set yet for the current transaction (which will be the case for the first call) or the stickied pool was unavailable so call to sticky was skipped. Empty should always lead to retry down the line if possible, otherwise a TPENOENT response.
     * @throws ResourceException Some softer errors are reported as resource exceptions. If these are thrown later retries with other pools is possible.
     */
    private <T> Optional<T> handleTransactionSticky(
            String serviceName,
            List<ConnectionFactoryEntry> factories,
            FunctionThrowsResourceException<CasualConnection, T> doCall) throws ResourceException
    {
        if (! TransactionPoolMapper.getInstance().isPoolMappingActive())
        {
            return Optional.empty();
        }

        String transactionPoolName = TransactionPoolMapper.getInstance().getPoolNameForCurrentTransaction();

        Optional<ConnectionFactoryEntry> stickyFactoryMaybe = factories
                .stream()
                .filter(connectionFactoryEntry -> connectionFactoryEntry.isValid() && connectionFactoryEntry.getJndiName().equals(transactionPoolName))
                .findFirst();

        if (stickyFactoryMaybe.isPresent() && stickyFactoryMaybe.get().isValid())
        {
            // We have a specific stickied pool to use that looks usable, try to use it
            ConnectionFactoryEntry connectionFactoryEntry = stickyFactoryMaybe.get();
            factories.remove(connectionFactoryEntry); // If we later need to do failover stuff we don't want to retry with this one
            LOG.finest(() -> "Attempting to use pool=" + connectionFactoryEntry.getJndiName() + " with sticky to current transaction.");

            try (CasualConnection con = connectionFactoryEntry.getConnectionFactory().getConnection())
            {
                return Optional.of(doCall.apply(con));
            }
            catch (CasualConnectionException e)
            {
                //This error branch will most likely happen if there are connection errors during a service call
                connectionFactoryEntry.invalidate();

                // These exceptions are rollback-only, do not attempt any retries.
                throw new CasualResourceException("Call failed during execution to service=" + serviceName
                        + " on connection=" + connectionFactoryEntry.getJndiName()
                        + " because of a network connection error, retries not possible.", e);
            }
        }
        else if (stickyFactoryMaybe.isPresent())
        {
            TransactionPoolMapper.getInstance().purgeMappingsForSpecificPool(transactionPoolName);
        }

        LOG.finest(() -> "Failed to call service=" + serviceName + " on stickied pool=" + transactionPoolName + ", falling through to normal flow.");

        // Found nothing, empty result should be signal to try normal flow.
        return Optional.empty();
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