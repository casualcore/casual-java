package se.laz.casual.jca;

import javax.faces.bean.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class DomainHandler
{
    private static final Logger log = Logger.getLogger(DomainHandler.class.getName());
    private Map<Address, Map<DomainId, AtomicInteger>> domainIds = new ConcurrentHashMap<>();
    private Map<Address, List<CasualConnectionListener>> connectionListeners = new ConcurrentHashMap<>();

    public boolean addDomainId(Address address, DomainId domainId)
    {
        log.warning(() -> "Adding domainId: " + domainId);
        domainIds.computeIfAbsent(address, key -> {
          Map<DomainId, AtomicInteger> value = new ConcurrentHashMap<>();
          value.put(domainId, new AtomicInteger(0));
          return value;
        });
        return domainIds.get(address).get(domainId).incrementAndGet() == 1;
    }

    public List<DomainId> getDomainIds(Address address)
    {
        return domainIds.get(address).keySet().stream().collect(Collectors.toList());
    }

    public void addConnectionListener(Address address, CasualConnectionListener listener)
    {
        connectionListeners.computeIfAbsent(address, key -> {
            List<CasualConnectionListener> listeners = new ArrayList<>();
            return listeners;
        }).add(listener);
    }

    public void removeConnectionListener(Address address, CasualConnectionListener listener)
    {
        List<CasualConnectionListener> listeners = connectionListeners.get(address);
        listeners.remove(listener);
        if(listeners.isEmpty())
        {
            connectionListeners.remove(address);
        }
    }

    public void handleNewConnection(Address address, DomainId domainId)
    {
        connectionListeners.get(address).forEach(listener -> listener.newConnection(domainId));
    }

    public void domainDisconnect(Address address, DomainId domainId)
    {
        if(0 == domainIds.get(address).get(domainId).decrementAndGet())
        {
            log.warning(() -> "Removing domainId: " + domainId);
            domainIds.remove(address);
            handleConnectionGone(address, domainId);
        }
    }

    private void handleConnectionGone(Address address, DomainId domainId)
    {
        connectionListeners.get(address).forEach((listener) -> listener.connectionGone(domainId));
    }
}


