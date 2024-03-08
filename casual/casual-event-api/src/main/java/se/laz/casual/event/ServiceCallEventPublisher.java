package se.laz.casual.event;

import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.util.PrettyPrinter;
import se.laz.casual.jca.RuntimeInformation;

import javax.transaction.xa.Xid;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceCallEventPublisher
{
    private static final Logger log = Logger.getLogger(ServiceCallEventPublisher.class.getName());
    private final ServiceCallEventHandler handlerSupplier;

    private ServiceCallEventPublisher(ServiceCallEventHandler handlerSupplier)
    {
        this.handlerSupplier = handlerSupplier;
    }

    public static ServiceCallEventPublisher of(ServiceCallEventHandler handlerSupplier)
    {
        Objects.requireNonNull(handlerSupplier, "handlerSupplier can not be null");
        return new ServiceCallEventPublisher(handlerSupplier);
    }

    /**
     *
     *  Only posts event in the case that the event server is up and running
     *  If not, it does nothing
     *
     * @param xid The transaction id
     * @param execution The execution
     * @param parentName The parent name
     * @param serviceName The service name
     * @param code The state for the service call
     * @param pendingMicroseconds The pending time in microseconds
     * @param start The start time
     * @param end The end time
     * @param order The order
     */
    public  void createAndPostEvent(Xid xid, UUID execution, String parentName, String serviceName, ErrorState code, long pendingMicroseconds, Instant start, Instant end, Order order)
    {
        Objects.requireNonNull(xid, "xid can not be null");
        Objects.requireNonNull(execution, "execution can not be null");
        Objects.requireNonNull(parentName, "parentName can not be null");
        Objects.requireNonNull(serviceName, "serviceName can not be null");
        Objects.requireNonNull(code, "code can not be null");
        Objects.requireNonNull(start, "start can not be null");
        Objects.requireNonNull(end, "end can not be null");
        Objects.requireNonNull(order, "order can not be null");
        Optional<ServiceCallEvent> event = createEvent(xid, execution, parentName, serviceName, code, pendingMicroseconds, start, end, order);
        event.ifPresent(this::post);
    }

    private Optional<ServiceCallEvent> createEvent(Xid xid, UUID execution, String parentName, String serviceName, ErrorState code, long pendingMicroseconds, Instant start, Instant end, Order order)
    {
        if(!RuntimeInformation.isEventServerStarted())
        {
            return Optional.empty();
        }
        try
        {
            return Optional.of(ServiceCallEvent.createBuilder()
                                               .withTransactionId(xid)
                                               .withExecution(execution)
                                               .withService(serviceName)
                                               .withCode(code)
                                               .withStart(ChronoUnit.MICROS.between(Instant.EPOCH, start))
                                               .withEnd(ChronoUnit.MICROS.between(Instant.EPOCH, end))
                                               .withOrder(order)
                                               .withPending(pendingMicroseconds)
                                               .withParent(parentName)
                                               .build());
        }
        catch(Exception anythingElse)
        {
            log.log(Level.WARNING, anythingElse, () -> creationErrorMessage(parentName, xid, execution, serviceName, code, start, end, order));
        }
        return Optional.empty();
    }

    private String creationErrorMessage(String parentName, Xid xid, UUID execution, String serviceName, ErrorState code, Instant start, Instant end, Order order)
    {
        return String.format("Failed to create ServiceCallEvent for service: %s, parent: %s, xid: %s, execution: %s, code: %s, start: %s, end: %s, order: %s",
                serviceName, parentName, PrettyPrinter.casualStringify(xid), PrettyPrinter.casualStringify(execution), code.name(), start, end, order.getValue());
    }

    private void post(ServiceCallEvent event)
    {
        try
        {
            handlerSupplier.put(event);
        }
        catch(NoServiceCallEventHandlerFoundException e)
        {
            log.log(Level.WARNING, e, () -> "Failed to get service call event handler metrics will not be available  " + event);
        }
        catch(Exception ee)
        {
            log.log(Level.WARNING, ee, () -> "Failed to post service call event - metrics will not be available for " + event);
        }
    }
}
