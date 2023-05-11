package se.laz.casual.network.outbound;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TransactionInformation
{
    private static final Logger LOG = Logger.getLogger(TransactionInformation.class.getName());
    private TransactionManager transactionManager;
    private Object lock = new Object();
    private List<Transaction> transactions = new ArrayList<>();

    public static TransactionInformation of()
    {
        return new TransactionInformation();
    }

    public boolean transactionsInFlight()
    {
        pruneTransactions();
        return !transactions.isEmpty();
    }

    public void pruneTransactions()
    {
        synchronized (lock)
        {
            transactions = transactions.stream()
                                       .filter(TransactionInformation::isInFlight)
                                       .collect(Collectors.toList());
            LOG.info(() -> "pruneTransactions: current number of transactions: " + transactions.size());
        }
    }

    public int numberOfCurrentTransactions()
    {
        pruneTransactions();
        return transactions.size();
    }

    private static boolean isInFlight(Transaction transaction)
    {
        try
        {
            TransactionStatus transactionStatus = TransactionStatus.unmarshal(transaction.getStatus());
            return  transactionStatus == TransactionStatus.STATUS_ACTIVE ||
                    transactionStatus == TransactionStatus.STATUS_COMMITTING ||
                    transactionStatus == TransactionStatus.STATUS_PREPARING ||
                    transactionStatus == TransactionStatus.STATUS_ROLLING_BACK;
        }
        catch (SystemException e)
        {
            // transaction.getStatus() failed, thus we lack information, we are then being negative default
            return false;
        }
    }

    public void addCurrentTransaction()
    {
        getCurrentTransaction().ifPresent(transaction -> {
            synchronized (lock)
            {
                transactions.add(transaction);
            }
        });
        LOG.info(() -> "addCurrentTransaction: current number of transactions: " + transactions.size());
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

    public void clear()
    {
        transactions.clear();
    }
}
