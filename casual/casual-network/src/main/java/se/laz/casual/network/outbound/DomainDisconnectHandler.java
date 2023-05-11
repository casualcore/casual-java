package se.laz.casual.network.outbound;

import se.laz.casual.network.connection.CasualConnectionException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DomainDisconnectHandler implements DomainDisconnectListener
{
    private static final Map<NettyNetworkConnection, UUID> disconnectedDomains = new ConcurrentHashMap<>();
    private final Map<NettyNetworkConnection, TransactionInformation> transactionInformation = new ConcurrentHashMap<>();

    private DomainDisconnectHandler()
    {}

    public static DomainDisconnectHandler of()
    {
        return new DomainDisconnectHandler();
    }

    @Override
    public void domainDisconnecting(NettyNetworkConnection networkConnection, UUID execution)
    {
        Objects.requireNonNull(networkConnection, "networkConnection can not be null");
        Objects.requireNonNull(execution, "execution can not be null");
        disconnectedDomains.put(networkConnection, execution);
    }

    public UUID getExecution(NettyNetworkConnection networkConnection)
    {
        Objects.requireNonNull(networkConnection, "networkConnection can not be null");
        return Optional.ofNullable(disconnectedDomains.get(networkConnection))
                       .orElseThrow(() -> new CasualConnectionException(""));
    }

    public boolean hasDomainBeenDisconnected(NettyNetworkConnection networkConnection)
    {
        Objects.requireNonNull(networkConnection, "networkConnection can not be null");
        return null != disconnectedDomains.get(networkConnection);
    }

    public void removeDomain(NettyNetworkConnection networkConnection)
    {
        Objects.requireNonNull(networkConnection, "networkConnection can not be null");
        disconnectedDomains.remove(networkConnection);
        transactionInformation.remove(networkConnection);
    }

    public void addCurrentTransaction(NettyNetworkConnection networkConnection)
    {
        Objects.requireNonNull(networkConnection, "networkConnection can not be null");
        Optional.ofNullable(transactionInformation.get(networkConnection))
                .orElseThrow(() -> new CasualConnectionException(""))
                .pruneTransactions()
                .addCurrentTransaction();
    }

    public boolean transactionsInfFlight(NettyNetworkConnection networkConnection)
    {
        Objects.requireNonNull(networkConnection, "networkConnection can not be null");
        return Optional.ofNullable(transactionInformation.get(networkConnection))
                       .orElseThrow(() -> new CasualConnectionException(""))
                       .transactionsInFlight();
    }

    public boolean domainDisconnectedButThereArePendingTransactions(NettyNetworkConnection nettyNetworkConnection)
    {
        return hasDomainBeenDisconnected(nettyNetworkConnection) || transactionsInfFlight(nettyNetworkConnection);
    }
}
