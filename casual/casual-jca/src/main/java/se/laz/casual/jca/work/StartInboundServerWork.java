/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.work;

import se.laz.casual.jca.InboundStartupException;
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceRegistry;
import se.laz.casual.network.inbound.CasualServer;
import se.laz.casual.network.inbound.ConnectionInformation;

import javax.resource.spi.work.Work;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Work instance for delaying start of inbound server until all startup services have been registered
 */
public final class StartInboundServerWork implements Work
{
    private static Logger log = Logger.getLogger( StartInboundServerWork.class.getName());
    private final List<String> startupServices;
    private final ConnectionInformation connectionInformation;
    private final Consumer<CasualServer> consumer;

    private StartInboundServerWork( List<String> startupServices, ConnectionInformation connectionInformation, Consumer<CasualServer> consumer )
    {
        this.startupServices = startupServices;
        this.connectionInformation = connectionInformation;
        this.consumer = consumer;
    }

    public static Work of(List<String> startupServices, ConnectionInformation connectionInformation, Consumer<CasualServer> consumer )
    {
        Objects.requireNonNull(startupServices, "Startup Services is null.");
        Objects.requireNonNull(connectionInformation, "Connection Information is null.");
        Objects.requireNonNull(consumer, "Consumer is null.");
        return new StartInboundServerWork(startupServices, connectionInformation, consumer);
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
        log.info(() -> "About to create casual inbound server");
        CasualServer server = CasualServer.of(connectionInformation);
        consumer.accept(server);
        log.info(() -> "Casual inbound server bound to port: " + connectionInformation.getPort());
    }

}
