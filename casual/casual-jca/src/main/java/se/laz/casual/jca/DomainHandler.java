/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DomainHandler
{
    private static final Logger log = Logger.getLogger(DomainHandler.class.getName());
    // Note:
    // This construct is since that for an address it can all be the same domain id per connection.
    // They may also all be different, or a mix - we do not know.
    private Map<Address, List<DomainIdReferenceCounted>> domainIds = new ConcurrentHashMap<>();
    private Object domainLock = new Object();
    private static final DomainHandler instance = new DomainHandler();

    private DomainHandler()
    {}

    public static DomainHandler getInstance()
    {
        return instance;
    }

    public void addDomainId(Address address, DomainId domainId)
    {
        synchronized (domainLock)
        {
            log.info(() -> "addDomain: " + address + " , " + domainId);
            domainIds.putIfAbsent(address, new ArrayList<>());
            List<DomainIdReferenceCounted> items = domainIds.get(address);
            DomainIdReferenceCounted domainIdReferenceCounted = items.stream()
                                                                     .filter(value -> value.getDomainId().equals(domainId))
                                                                     .findFirst()
                                                                     .orElse(null);
            if (null == domainIdReferenceCounted)
            {
                log.info(() -> "adding new domainId: " + domainId);
                domainIdReferenceCounted = DomainIdReferenceCounted.of(domainId);
                items.add(domainIdReferenceCounted);
            }
            // First time seen? ( As a domain id for an address can be seen as many times as there are connections)
            // Note, this is not just a side effect, we want to up the reference count
            if (domainIdReferenceCounted.incrementAndGet() == 1)
            {
                log.info(() -> "new domain id: " + domainId + " for address: " + address);
            }
        }
    }

    public List<DomainId> getDomainIds(Address address)
    {
        List<DomainIdReferenceCounted> values = domainIds.get(address);
        if (null != values)
        {
            return values.stream()
                         .map(DomainIdReferenceCounted::getDomainId)
                         .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public void domainDisconnect(Address address, DomainId domainId)
    {
        synchronized (domainLock)
        {
            log.info(() -> "domainDisconnect: " + address + " , " + domainId);
            List<DomainIdReferenceCounted> domainIdsPerAddress = domainIds.get(address);
            if (null != domainIdsPerAddress)
            {
                DomainIdReferenceCounted domainIdReferenceCounted = domainIdsPerAddress.stream()
                                                                                       .filter(value -> value.getDomainId().equals(domainId))
                                                                                       .findFirst()
                                                                                       .orElse(null);
                if(null == domainIdReferenceCounted)
                {
                    log.warning(() -> "unknown domain disconnect for address: " + address + " domainId: " + domainId + " this should never happen");
                    return;
                }
                if (domainIdReferenceCounted.decrementAndGet() == 0)
                {
                    log.info(() -> "domain id gone - removing: " + domainId);
                    domainIdsPerAddress.remove(domainIdReferenceCounted);
                    if(domainIdsPerAddress.isEmpty())
                    {
                        log.info(() -> "no more domain ids for address: " + address + "  - removing");
                        domainIds.remove(address);
                    }
                }
            }
        }
    }
}
