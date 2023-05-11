package se.laz.casual.network.outbound;

import se.laz.casual.network.connection.CasualConnectionException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransactionManagerProvider
{
     // wls/wildfly names
    private static final List<String> TRANSACTION_MANAGER_NAMES = Arrays.asList("javax.transaction.TransactionManager","java:/TransactionManager");
    TransactionManager getTransactionManager()
    {
        try
        {
            InitialContext context = new InitialContext();
            return TRANSACTION_MANAGER_NAMES.stream()
                                            .map(name -> getTransactionManagerByName(name, context).orElse(null))
                                            .filter( Objects::nonNull)
                                            .findFirst()
                                            .orElseThrow(() -> new CasualConnectionException("No transaction manager found for this application server. CasualCaller will not function! Potential candidates were: " + TRANSACTION_MANAGER_NAMES));
        }
        catch (NamingException e)
        {
            throw new CasualConnectionException("InitialContext could not be created, CasualCaller will not function!", e);
        }
    }

    private Optional<TransactionManager> getTransactionManagerByName(String transactionManagerName, InitialContext context)
    {
        try
        {
            return Optional.of((TransactionManager) context.lookup(transactionManagerName));
        }
        catch (NamingException e)
        {
            // NOP
        }
        return Optional.empty();
    }

}
