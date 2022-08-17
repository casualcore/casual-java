package se.laz.casual.jca;

import javax.faces.bean.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class DomainHandler
{
    private static final Logger log = Logger.getLogger(DomainHandler.class.getName());
    // Note:
    // This construct is since that for an address it can all be the same domain id per connection.
    // They may also all be different, or a mix - we do not know.
    private Map<Address, List<DomainIdReferenceCounted>> domainIds = new ConcurrentHashMap<>();
    private Map<Address, List<CasualConnectionListener>> connectionListeners = new ConcurrentHashMap<>();
    private Object domainLock = new Object();
    private Object listenerLock = new Object();


    public void addDomainId(Address address, DomainId domainId)
    {
        synchronized (domainLock)
        {
            domainIds.putIfAbsent(address, new ArrayList<>());
            List<DomainIdReferenceCounted> items = domainIds.get(address);
            DomainIdReferenceCounted domainIdReferenceCounted = items.stream()
                                                                     .filter(value -> value.getDomainId().equals(domainId))
                                                                     .findFirst()
                                                                     .orElse(null);
            if (null == domainIdReferenceCounted)
            {
                log.info(() -> "Adding domainId: " + domainId);
                domainIdReferenceCounted = DomainIdReferenceCounted.of(domainId);
                items.add(domainIdReferenceCounted);
            }
            // First time seen? ( As a domain id for an address can be seen as many times as there are connections)
            if (domainIdReferenceCounted.incrementAndGet() == 1)
            {
                handleNewConnection(address, domainId);
            }
        }
    }

    public List<DomainId> getDomainIds(Address address)
    {
        List<DomainIdReferenceCounted> values = domainIds.get(address);
        if(null != values)
        {
            return values.stream()
                         .map(value -> value.getDomainId())
                         .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public void addConnectionListener(Address address, CasualConnectionListener listener)
    {
        synchronized (listenerLock)
        {
            List<CasualConnectionListener> listenersForAddress = connectionListeners.computeIfAbsent(address, key -> {
                List<CasualConnectionListener> listeners = new ArrayList<>();
                return listeners;
            });
            if (!listenersForAddress.contains(listener))
            {
                listenersForAddress.add(listener);
            }
        }
    }

    public void removeConnectionListener(Address address, CasualConnectionListener listener)
    {
        synchronized (listenerLock)
        {
            List<CasualConnectionListener> listeners = connectionListeners.get(address);
            listeners.remove(listener);
            if (listeners.isEmpty())
            {
                connectionListeners.remove(address);
            }
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

    public void domainDisconnect(Address address, DomainId domainId)
    {
        synchronized (domainLock)
        {
            List<DomainIdReferenceCounted> domainIdsPerAddress = domainIds.get(address);
            if (null != domainIdsPerAddress)
            {
                DomainIdReferenceCounted domainIdReferenceCounted = domainIdsPerAddress.stream()
                                                                                       .filter(value -> value.getDomainId().equals(domainId))
                                                                                       .findFirst()
                                                                                       .orElse(null);
                if (null == domainIdReferenceCounted)
                {
                    throw new CasualResourceAdapterException("unknown domain disconnect for address: " + address + " domainId: " + domainId + " this should never happen");
                }
                if (domainIdReferenceCounted.decrementAndGet() == 0)
                {
                    log.info(() -> "domain id gone: " + domainId);
                    domainIds.remove(domainIdReferenceCounted);
                    handleConnectionGone(address, domainId);
                }
            }
        }
    }

    private void handleConnectionGone(Address address, DomainId domainId)
    {
        connectionListeners.get(address).forEach((listener) -> listener.connectionGone(domainId));
    }
}


