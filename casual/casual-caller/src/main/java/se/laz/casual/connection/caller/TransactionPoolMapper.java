/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.CasualRuntimeException;
import se.laz.casual.connection.caller.config.ConfigurationService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class TransactionPoolMapper
{
    private static final Logger LOG = Logger.getLogger(TransactionPoolMapper.class.getName());
    private final Map<Transaction, String> transactionStickies = new ConcurrentHashMap<>();

    private boolean stickyEnabled = ConfigurationService.getInstance().getConfiguration().isTransactionStickyEnabled();
    private final Object lock = new Object();

    TransactionManager transactionManager;

    TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    private static TransactionPoolMapper instance;

    // For test
    static void resetForTest()
    {
        instance = null;
    }

    void setActiveForTest(boolean isActive)
    {
        this.stickyEnabled = isActive;
    }

    void setTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    private TransactionPoolMapper()
    {
        // NOP
    }

    public static synchronized TransactionPoolMapper getInstance()
    {
        if (instance == null)
        {
            instance = new TransactionPoolMapper();
        }

        return instance;
    }

    public String getPoolNameForCurrentTransaction()
    {
        if (!stickyEnabled)
        {
            return null;
        }

        Optional<Transaction> transactionMaybe = getCurrentTransaction();
        return transactionMaybe.map(transactionStickies::get).orElse(null);
    }

    public void setPoolNameForCurrentTransaction(String poolName)
    {
        if (!stickyEnabled)
        {
            return;
        }

        Optional<Transaction> transactionMaybe = getCurrentTransaction();

        transactionMaybe.ifPresent(transaction ->
        {
            synchronized (lock)
            {
                if (transactionStickies.containsKey(transaction))
                {
                    throw new CasualRuntimeException("Attempted to set a pool sticky ("
                            + poolName + ") for a transaction that was already stickied to another pool ("
                            + transactionStickies.get(transaction) + ").");
                }
                else
                {
                    transactionStickies.put(transaction, poolName);
                    ensureTransactionPruningOnCompletion(transaction);
                }
            }
        });
    }

    private void ensureTransactionPruningOnCompletion(Transaction transaction)
    {
        Objects.requireNonNull(transaction, "Must supply transaction to handle.");

        getTransactionSynchronizationRegistry().ifPresent(registry -> registry.registerInterposedSynchronization(new Synchronization()
        {
            @Override
            public void beforeCompletion()
            {
                transactionStickies.remove(transaction);
            }

            @Override
            public void afterCompletion(int status)
            {
                // NOP
            }
        }));
    }

    private Optional<TransactionSynchronizationRegistry> getTransactionSynchronizationRegistry()
    {
        if (transactionSynchronizationRegistry == null)
        {
            try
            {
                transactionSynchronizationRegistry = InitialContext.doLookup("java:comp/TransactionSynchronizationRegistry");
            }
            catch (NamingException e)
            {
                LOG.severe("Failed to load TransactionSynchronizationRegistry, will not be able to remove registered transaction pool mappings on completion.");
            }
        }

        return Optional.ofNullable(transactionSynchronizationRegistry);
    }

    private Optional<Transaction> getCurrentTransaction()
    {
        if (transactionManager == null)
        {
            transactionManager = new TransactionManagerProvider().getTransactionManager();
        }

        try
        {
            return Optional.ofNullable(transactionManager.getTransaction());
        }
        catch (SystemException e)
        {
            return Optional.empty();
        }
    }

    public int getNumberOfTrackedTransactions()
    {
        return transactionStickies.size();
    }

    public boolean isPoolMappingActive()
    {
        return stickyEnabled;
    }

    public void purgeMappingsForSpecificPool(String poolName)
    {
        synchronized (lock)
        {
            Set<Transaction> keys = transactionStickies.keySet();
            for (Transaction key : keys)
            {
                String entry = transactionStickies.get(key);
                if (poolName.equals(entry))
                {
                    transactionStickies.remove(key);
                }
            }
        }
    }
}
