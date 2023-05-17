package se.laz.casual.network.outbound;

import java.util.Objects;
import java.util.UUID;

public class DomainDisconnectHandler implements DomainDisconnectListener
{
    private UUID execution;
    private TransactionInformation transactionInformation;

    private DomainDisconnectHandler()
    {}

    public static DomainDisconnectHandler of()
    {
        return new DomainDisconnectHandler();
    }

    @Override
    public void domainDisconnecting(UUID execution)
    {
        Objects.requireNonNull(execution, "execution can not be null");
        this.execution = execution;
    }

    public UUID getExecution()
    {
        return execution;
    }

    public boolean hasDomainBeenDisconnected()
    {
        return null != execution;
    }

    public void addCurrentTransaction()
    {
        if(null == transactionInformation)
        {
            transactionInformation = TransactionInformation.of();
        }
        transactionInformation.addCurrentTransaction();
    }

    public boolean transactionsInfFlight()
    {
        return null != transactionInformation && transactionInformation.transactionsInFlight();
    }
}
