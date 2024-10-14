/*
 * Copyright (c) 2021 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.work;

import jakarta.resource.spi.work.Work;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.jca.InboundStartupException;
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Work instance for delaying start of inbound server until all startup services have been registered
 */
public final class StartInboundServerWork<T> implements Work
{
    private static Logger log = Logger.getLogger( StartInboundServerWork.class.getName());
    private final List<String> startupServices;
    private final Consumer<T> consumer;
    private final Supplier<T> supplier;
    private final Supplier<String> logMessage;
    private long delay;

    private StartInboundServerWork( List<String> startupServices, Supplier<String> logMessage, Consumer<T> consumer, Supplier<T> supplier, long delay)
    {
        this.startupServices = startupServices;
        this.consumer = consumer;
        this.supplier = supplier;
        this.logMessage = logMessage;
        this.delay = delay;
    }

    public static <T> Work of(List<String> startupServices, Supplier<String> logMessage, Consumer<T> consumer, Supplier<T> supplier )
    {
        return of(startupServices, logMessage, consumer, supplier, 0);
    }

    public static <T> Work of(List<String> startupServices, Supplier<String> logMessage, Consumer<T> consumer, Supplier<T> supplier, long delay )
    {
        Objects.requireNonNull(startupServices, "Startup Services is null.");
        Objects.requireNonNull(consumer, "Consumer is null.");
        Objects.requireNonNull(supplier, "supplier is null");
        // note: only needed to make the compiler, java 8, not spew out warnings
        StartInboundServerWork<T> work = new StartInboundServerWork<>(startupServices, logMessage, consumer, supplier, delay);
        return work;
    }

    @Override
    public void release()
    {
        /**
         * We can not speed up the completion
         */
    }

    @Override
    public void run()
    {
        waitForInboundStartupServices();
        maybeDelay();
        startInboundServer();
    }

    private void waitForInboundStartupServices()
    {
        logInitialStartupServices(startupServices);
        Set<String> remaining = checkRemainingServices( new HashSet<>( this.startupServices ) );
        while(!remaining.isEmpty())
        {
            int previousNumberOfServicesRemaining = remaining.size();
            remaining = checkRemainingServices( remaining );
            if(!remaining.isEmpty() && remaining.size() < previousNumberOfServicesRemaining)
            {
                logWaitingForStartupServices(remaining);
            }
            try
            {
                Thread.sleep( 1000 );
            }
            catch( InterruptedException e )
            {
                Thread.currentThread().interrupt();
                throw new InboundStartupException( "Interrupted waiting for inbound startup services registration.", e );
            }
        }
        log.info(() -> "All startup services registered.");
    }

    private void logInitialStartupServices(List<String> startupServices)
    {
        log.info(() -> "Inbound startup mode: " + ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE ) );
        log.info(() -> "Waiting for " + startupServices.size() + " services to be registered before inbound starts.");
        log.info(() -> "Initial services list: " + startupServices.stream()
                                                                  .collect(Collectors.joining()));
    }

    private void logWaitingForStartupServices(Set<String> remaining)
    {
        log.info(() -> "Waiting for registration of the following services: " + remaining.stream()
                                                                                         .collect(Collectors.joining()));
    }

    private Set<String> checkRemainingServices( Set<String> remaining )
    {
        CasualServiceRegistry registry = CasualServiceRegistry.getInstance();
        Map<Boolean,Set<String>> found = remaining.stream()
                .collect( Collectors.partitioningBy( registry::hasServiceEntry, Collectors.toSet() ) );
        for( String foundService: found.get( true ) )
        {
            log.info( ()-> "Startup service registered: " + foundService );
        }
        return found.get( false );
    }

    private void maybeDelay()
    {
        // Note:
        // This, optional, delay of inbound is due to an issue on wls where once in a blue moon - the MessageEndpointFactory is in fact not ready
        // and there are incoming domain discoveries ( during restart)
        // That will then fail and continue to fail forever, even after endpointActivation for the resource adapter has completed - it should heal
        // This is a horrible "work around" for that specific problem - the user decides how long inbound startup has to be delayed
        // Never seen on wildfly
        if(delay <= 0L)
        {
            log.info(() -> "no inbound startup delay");
            return;
        }
        Delayer.delay(delay);
        log.info(() -> "inbound startup, delay of " + delay + " seconds - done");
    }

    private void startInboundServer()
    {
        consumer.accept(supplier.get());
        log.info(logMessage::get);
    }

}
