package se.laz.casual.jca;

import javax.faces.bean.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class DomainHandler
{
    private static final Logger log = Logger.getLogger(DomainHandler.class.getName());
    // Note:
    // This construct is since that for an address it can all be the same domain id per connection.
    // They may also all be different - we do not know.
    private Map<Address, List<Map<DomainId, AtomicInteger>>> domainIds = new ConcurrentHashMap<>();
    private Map<Address, List<CasualConnectionListener>> connectionListeners = new ConcurrentHashMap<>();

    public synchronized boolean addDomainId(Address address, DomainId domainId)
    {
        log.warning(() -> "Adding domainId: " + domainId);
        domainIds.putIfAbsent(address, new ArrayList<>());
        List<Map<DomainId, AtomicInteger>> items = domainIds.get(address);
        AtomicInteger atomicInteger  = items.stream()
                                            .filter(value -> null != value.get(domainId))
                                            .map(value -> value.get(domainId))
                                            .findFirst()
                                            .orElse(null);
        if(null == atomicInteger)
        {
            atomicInteger = new AtomicInteger(0);
            Map<DomainId, AtomicInteger> item = new ConcurrentHashMap<>();
            item.put(domainId, atomicInteger);
            items.add(item);
        }
        // First time seen? ( As a domain id for an address can be seen as many times as there are connections)
        return atomicInteger.incrementAndGet() == 1;
    }

    public List<DomainId> getDomainIds(Address address)
    {
        List<Map<DomainId, AtomicInteger>> values = domainIds.get(address);
        if(null != values)
        {
            return values.stream()
                         .map(value -> value.keySet().stream().collect(Collectors.toList()))
                         .flatMap(Collection::stream)
                         .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public synchronized void addConnectionListener(Address address, CasualConnectionListener listener)
    {
        List<CasualConnectionListener> listenersForAddress = connectionListeners.computeIfAbsent(address, key -> {
            List<CasualConnectionListener> listeners = new ArrayList<>();
            return listeners;
        });
        if(!listenersForAddress.contains(listener))
        {
            listenersForAddress.add(listener);
        }
    }

    public synchronized void removeConnectionListener(Address address, CasualConnectionListener listener)
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
        List<CasualConnectionListener> listeners = connectionListeners.get(address);
        if(null != listeners)
        {
            listeners.forEach(listener -> listener.newConnection(domainId));
        }
    }

    public synchronized void domainDisconnect(Address address, DomainId domainId)
    {
        List<Map<DomainId, AtomicInteger>> domainIdsPerAddress = domainIds.get(address);
        if(null != domainIdsPerAddress )
        {
            Optional<Map<DomainId, AtomicInteger>> maybeValue = domainIdsPerAddress.stream()
                                                                                   .filter(value -> null != value.get(domainId))
                                                                                   .findFirst();
            maybeValue.ifPresent( value -> {
                AtomicInteger atomicInteger = value.get(domainId);
                if(null != atomicInteger)
                {
                    if(0 == atomicInteger.decrementAndGet())
                    {
                        log.warning(() -> "Removing domainId: " + domainId);
                        domainIdsPerAddress.remove(value);
                        handleConnectionGone(address, domainId);
                    }
                }
                else
                {
                    // TODO: at least log this, very unexpected
                }
            });
        }
    }

    private void handleConnectionGone(Address address, DomainId domainId)
    {
        connectionListeners.get(address).forEach((listener) -> listener.connectionGone(domainId));
    }
}


