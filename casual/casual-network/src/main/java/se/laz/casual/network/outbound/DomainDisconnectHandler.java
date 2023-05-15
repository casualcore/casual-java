package se.laz.casual.network.outbound;

import se.laz.casual.network.connection.CasualConnectionException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DomainDisconnectHandler implements DomainDisconnectListener
{
    private static final Map<NetworkConnectionId, UUID> disconnectedDomains = new ConcurrentHashMap<>();
    private final Map<NetworkConnectionId, TransactionInformation> transactionInformation = new ConcurrentHashMap<>();

    private DomainDisconnectHandler()
    {}

    public static DomainDisconnectHandler of()
    {
        return new DomainDisconnectHandler();
    }

    @Override
    public void domainDisconnecting(NetworkConnectionId id, UUID execution)
    {
        Objects.requireNonNull(id, "id can not be null");
        Objects.requireNonNull(execution, "execution can not be null");
        disconnectedDomains.put(id, execution);
    }

    public UUID getExecution(NetworkConnectionId id)
    {
        Objects.requireNonNull(id, "id can not be null");
        return Optional.ofNullable(disconnectedDomains.get(id))
                       .orElseThrow(() -> new CasualConnectionException(""));
    }

    public boolean hasDomainBeenDisconnected(NetworkConnectionId id)
    {
        Objects.requireNonNull(id, "id can not be null");
        return null != disconnectedDomains.get(id);
    }

    public void removeDomain(NetworkConnectionId id)
    {
        Objects.requireNonNull(id, "id can not be null");
        disconnectedDomains.remove(id);
        transactionInformation.remove(id);
    }

    public void addCurrentTransaction(NetworkConnectionId id)
    {
        Objects.requireNonNull(id, "id can not be null");
        transactionInformation.computeIfAbsent(id, theId -> TransactionInformation.of())
                              .pruneTransactions()
                              .addCurrentTransaction();
    }

    public boolean transactionsInfFlight(NetworkConnectionId id)
    {
        Objects.requireNonNull(id, "id can not be null");
        return Optional.ofNullable(transactionInformation.get(id))
                       .orElseThrow(() -> new CasualConnectionException("No TransactionInformation for id: " + id + " this should never happen!"))
                       .transactionsInFlight();
    }
}
