/*
 * Copyright (c) 2021 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.work

import jakarta.resource.spi.XATerminator
import jakarta.resource.spi.endpoint.MessageEndpointFactory
import jakarta.resource.spi.work.Work
import jakarta.resource.spi.work.WorkManager
import se.laz.casual.config.Mode
import se.laz.casual.jca.InboundStartupException
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerFactory
import se.laz.casual.jca.inbound.handler.test.TestServiceHandler
import se.laz.casual.jca.inflow.CasualInboundTransactionRegistry
import se.laz.casual.network.inbound.CasualServer
import se.laz.casual.network.inbound.ConnectionInformation
import spock.lang.Specification

import java.util.concurrent.CompletionService
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class StartInboundServerWorkTest extends Specification
{
    ExecutorService service = Executors.newFixedThreadPool( 2 )
    CompletionService<Void> completionService = new ExecutorCompletionService<>( service )
    Work instance
    ConnectionInformation ci

    TestServiceHandler testHandler = (TestServiceHandler) ServiceHandlerFactory.getHandlers(  ).stream(  )
            .filter {  it instanceof TestServiceHandler }
            .findFirst(  )
            .orElseThrow( ()-> new RuntimeException( "pop" ))

    CasualServer server

    CasualInboundTransactionRegistry inboundTransactionRegistry

    def setup()
    {
        inboundTransactionRegistry = new CasualInboundTransactionRegistry()
        MessageEndpointFactory endpointFactory = Mock( MessageEndpointFactory )
        WorkManager workManager = Mock( WorkManager)
        XATerminator xaTerminator = Mock( XATerminator )
        ci = ConnectionInformation.createBuilder()
                .withFactory(endpointFactory)
                .withPort( 0 )
                .withWorkManager(workManager)
                .withXaTerminator(xaTerminator)
                .withInboundTransactionRegistry(inboundTransactionRegistry)
                .build()
    }

    def cleanup()
    {
        testHandler.clear(  )
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
        testHandler.registerService( serviceName1 )

        instance = StartInboundServerWork.of( [serviceName1], {"inbound started"},{s -> server = s}, {CasualServer.of(ci)})

        when:
        completionService.submit( { instance.run(  ) } )
        completionService.submit( {
            testHandler.makeServiceAvailable( serviceName1 )
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
        testHandler.registerService( serviceName1 )

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
        for( String serviceName: serviceNames )
        {
            testHandler.registerService( serviceName )
        }

        instance = StartInboundServerWork.of( serviceNames, {"inbound started"},{s -> server = s}, {CasualServer.of(ci)})

        when:
        completionService.submit( { instance.run(  ) } )
        completionService.submit( {
            for( String service: serviceNames )
            {
                testHandler.makeServiceAvailable( service )
                Thread.sleep( 1010 )
            }
        } )

        completionService.take(  ).get( 5, TimeUnit.SECONDS )
        completionService.take(  ).get( 5, TimeUnit.SECONDS )

        then:
        server.isActive(  )
    }
}
