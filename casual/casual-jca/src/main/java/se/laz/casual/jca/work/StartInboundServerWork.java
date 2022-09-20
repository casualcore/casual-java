/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.work;

import se.laz.casual.jca.InboundStartupException;
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceRegistry;

import javax.resource.spi.work.Work;
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

    private StartInboundServerWork( List<String> startupServices, Supplier<String> logMessage, Consumer<T> consumer, Supplier<T> supplier)
    {
        this.startupServices = startupServices;
        this.consumer = consumer;
        this.supplier = supplier;
        this.logMessage = logMessage;
    }

    public static <T> Work of(List<String> startupServices, Supplier<String> logMessage, Consumer<T> consumer, Supplier<T> supplier )
    {
        Objects.requireNonNull(startupServices, "Startup Services is null.");
        Objects.requireNonNull(consumer, "Consumer is null.");
        Objects.requireNonNull(supplier, "supplier is null");
        // note: only needed to make the compiler, java 8, not spew out warnings
        StartInboundServerWork<T> work = new StartInboundServerWork<>(startupServices, logMessage, consumer, supplier);
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
        startInboundServer();
    }

    private void waitForInboundStartupServices()
    {
        log.info(() -> "Waiting for " + startupServices.size() + " startup services to be registered before inbound starts.");
        Set<String> remaining = checkRemainingServices( new HashSet<>( this.startupServices ) );
        while(!remaining.isEmpty())
        {
            remaining = checkRemainingServices( remaining );
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

    private void startInboundServer()
    {
        consumer.accept(supplier.get());
        log.info(() -> logMessage.get());
    }

}
