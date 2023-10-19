/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.work

import se.laz.casual.api.service.CasualService
import se.laz.casual.config.Mode
import se.laz.casual.jca.InboundStartupException
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceEntry
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceLiteral
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceMetaData
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceRegistry
import se.laz.casual.network.inbound.CasualServer
import se.laz.casual.network.inbound.ConnectionInformation
import spock.lang.Shared
import spock.lang.Specification

import jakarta.resource.spi.XATerminator
import jakarta.resource.spi.endpoint.MessageEndpointFactory
import jakarta.resource.spi.work.Work
import jakarta.resource.spi.work.WorkManager
import java.lang.reflect.Method
import java.util.concurrent.CompletionService
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class StartInboundServerWorkTest extends Specification
{

    @Shared
    Integer port = 7772

    ExecutorService service = Executors.newFixedThreadPool( 2 )
    CompletionService<Void> completionService = new ExecutorCompletionService<>( service )
    Work instance
    ConnectionInformation ci

    CasualServiceRegistry registry = CasualServiceRegistry.getInstance()

    CasualServer server

    def setup()
    {
        MessageEndpointFactory endpointFactory = Mock( MessageEndpointFactory )
        WorkManager workManager = Mock( WorkManager)
        XATerminator xaTerminator = Mock( XATerminator )
        ci = ConnectionInformation.createBuilder()
                .withFactory(endpointFactory)
                .withPort( port )
                .withWorkManager(workManager)
                .withXaTerminator(xaTerminator)
                .build()
    }

    def cleanup()
    {
        registry.clear(  )
        if( server != null )
        {
            server.close(  )
        }
        service.shutdown(  )
    }

    def "Start service with no startup services"()
    {
        given:
        instance = StartInboundServerWork.of( [], {"inbound started"},{s -> server = s}, {CasualServer.of(ci)} )

        when:
        completionService.submit( { instance.run(  ) } )
        completionService.take(  ).get( 5, TimeUnit.SECONDS )

        then:
        server.isActive()
    }

    def "Start service with 1 startup services"()
    {
        given:
        String serviceName1 = Mode.Constants.TRIGGER_SERVICE
        CasualServiceEntry entry = prepareRegistry( serviceName1 )

        instance = StartInboundServerWork.of( [serviceName1], {"inbound started"},{s -> server = s}, {CasualServer.of(ci)})

        when:
        completionService.submit( { instance.run(  ) } )
        completionService.submit( {
            registry.register( entry )
        } )

        completionService.take(  ).get( 5, TimeUnit.SECONDS )
        completionService.take(  ).get( 5, TimeUnit.SECONDS )

        then:
        server.isActive(  )
    }

    def "Start service with 1 startup services, shutdown forces InterruptException, wrapped as InboundStartupException"()
    {
        given:
        String serviceName1 = Mode.Constants.TRIGGER_SERVICE
        prepareRegistry( serviceName1 )

        instance = StartInboundServerWork.of( [serviceName1], {"inbound started"}, {s -> server = s}, {CasualServer.of(ci)} )

        when:
        completionService.submit( { instance.run(  ) } )
        service.shutdownNow(  )

        try
        {
            completionService.take(  ).get(  )
        }
        catch( ExecutionException e )
        {
            throw e.getCause(  )
        }

        then:
        thrown InboundStartupException
    }

    def "Start service with 2 startup services"()
    {
        given:
        List<String> serviceNames = ["service1","service2"]
        List<CasualServiceEntry> entries = []
        for( String serviceName: serviceNames )
        {
            entries.add( prepareRegistry( serviceName ) )
        }

        instance = StartInboundServerWork.of( serviceNames, {"inbound started"},{s -> server = s}, {CasualServer.of(ci)})

        when:
        completionService.submit( { instance.run(  ) } )
        completionService.submit( {
            for( CasualServiceEntry entry: entries )
            {
                registry.register( entry )
                Thread.sleep( 1010 )
            }
        } )

        completionService.take(  ).get( 5, TimeUnit.SECONDS )
        completionService.take(  ).get( 5, TimeUnit.SECONDS )

        then:
        server.isActive(  )
    }

    CasualServiceEntry prepareRegistry( String serviceName )
    {
        CasualService service = new CasualServiceLiteral( serviceName, "" )
        Class<?> serviceClass = String.class
        Method serviceMethod = String.class.getMethod( "toString" )

        CasualServiceMetaData metaData = CasualServiceMetaData.newBuilder(  )
                .service( service )
                .implementationClass( serviceClass )
                .serviceMethod( serviceMethod )
                .build(  )

        registry.register( metaData )

        return CasualServiceEntry.of( serviceName, "", null, null )
    }

}
