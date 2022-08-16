package se.laz.casual.jca;

import javax.faces.bean.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@ApplicationScoped
public class DomainHandler
{
    Logger log = Logger.getLogger(DomainHandler.class.getName());
    private final Map<DomainId, AtomicInteger> domainIds = new ConcurrentHashMap<>();
    private final Map<CasualConnectionListener, Boolean> connectionListeners = new ConcurrentHashMap<>();

    public boolean addDomainId(DomainId domainId)
    {
        log.warning(() -> "Adding domainId: " + domainId);
        domainIds.putIfAbsent(domainId, new AtomicInteger(0));
        return domainIds.get(domainId).incrementAndGet() == 1;
    }



}
