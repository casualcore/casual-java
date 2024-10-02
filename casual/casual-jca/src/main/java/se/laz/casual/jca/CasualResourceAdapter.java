/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ConfigProperty;
import jakarta.resource.spi.Connector;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.TransactionSupport;
import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkException;
import jakarta.resource.spi.work.WorkListener;
import jakarta.resource.spi.work.WorkManager;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.config.ReverseInbound;
import se.laz.casual.event.server.EventServer;
import se.laz.casual.event.server.EventServerConnectionInformation;
import se.laz.casual.jca.inflow.CasualActivationSpec;
import se.laz.casual.jca.jmx.JMXStartup;
import se.laz.casual.jca.work.StartInboundServerListener;
import se.laz.casual.jca.work.StartInboundServerWork;
import se.laz.casual.jca.work.StartReverseInboundServerListener;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.inbound.CasualServer;
import se.laz.casual.network.inbound.ConnectionInformation;
import se.laz.casual.network.inbound.reverse.AutoConnect;
import se.laz.casual.network.inbound.reverse.ReverseInboundConnectionInformation;
import se.laz.casual.network.reverse.inbound.ReverseInboundListener;
import se.laz.casual.network.reverse.inbound.ReverseInboundServer;

import javax.transaction.xa.XAResource;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * CasualResourceAdapter
 *
 * @version $Revision: $
 */
@Connector(
        displayName = "Casual RA",
        vendorName = "Casual",
        eisType = "Casual",
        version = "1.0",
        transactionSupport = TransactionSupport.TransactionSupportLevel.XATransaction
)
public class CasualResourceAdapter implements ResourceAdapter, ReverseInboundListener
{
    private static Logger log = Logger.getLogger(CasualResourceAdapter.class.getName());
    private ConcurrentHashMap<Integer, CasualActivationSpec> activations = new ConcurrentHashMap<>();
    private List<ReverseInboundServer> reverseInbounds = new ArrayList<>();
    // it is not really unused, it should never ever be gc:ed, thus it is part of this class
    @SuppressWarnings("java:S1068")
    private EventServer eventServer;

    private WorkManager workManager;
    private XATerminator xaTerminator;

    private CasualServer server;

    @ConfigProperty( defaultValue = "7772")
    private Integer inboundServerPort;

    private ConfigurationService configurationService;

    public CasualResourceAdapter()
    {
        //JCA requires ResourceAdapter has a no arg constructor.
        //It is also not possible to inject with CDI on wildfly only ConfigProperty annotations.
        configurationService = ConfigurationService.getInstance();
        log.info(() -> "casual jca configuration: " + configurationService.getConfiguration());
        configurationService.getConfiguration().getEventServer().ifPresent(config -> {
            log.info("starting event server with config: " + config);
            eventServer = EventServer.of(EventServerConnectionInformation.createBuilder()
                    .withUseEpoll( config.isUseEpoll() )
                    .withPort( config.getPortNumber() )
                    .withShutdownTimeout( config.getShutdown().getTimeout() )
                    .withShutdownQuietPeriod( config.getShutdown().getQuietPeriod() )
                    .build(), configurationService.getConfiguration().getDomain().getId() );
            log.info("event server started at port: " + config.getPortNumber());
            RuntimeInformation.setEventServerStarted(true);
        });
    }



    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory,
                                   ActivationSpec spec) throws ResourceException
    {
        log.info(()->"start endpointActivation() ");
        CasualActivationSpec as = (CasualActivationSpec) spec;
        as.setPort( getInboundServerPort() );
        ConnectionInformation ci = ConnectionInformation.createBuilder()
                .withFactory(endpointFactory)
                .withPort(as.getPort())
                .withWorkManager(workManager)
                .withXaTerminator(xaTerminator)
                .withUseEpoll( configurationService.getConfiguration().getInbound().isUseEpoll() )
                .build();
        activations.put(as.getPort(), as);
        log.info(() -> "start casual inbound server" );
        startInboundServer( ci );
        maybeStartReverseInbound( ConfigurationService.getInstance().getConfiguration().getReverseInbound(), endpointFactory, workManager, xaTerminator);
        log.finest(() -> "end endpointActivation()");

    }

    private void maybeStartReverseInbound(List<ReverseInbound> reverseInbound, MessageEndpointFactory endpointFactory, WorkManager workManager, XATerminator xaTerminator)
    {
        for(ReverseInbound instance : reverseInbound)
        {
            startReverseInbound(ReverseInboundConnectionInformation.createBuilder()
                                                                   .withAddress(InetSocketAddress.createUnresolved(instance.getAddress().getHost(), instance.getAddress().getPort()))
                                                                   .withDomainId(configurationService.getConfiguration().getDomain().getId())
                                                                   .withDomainName(configurationService.getConfiguration().getDomain().getName())
                                                                   .withUseEpoll(configurationService.getConfiguration().getOutbound().getUseEpoll())
                                                                   .withFactory(endpointFactory)
                                                                   .withWorkManager(workManager)
                                                                   .withXaTerminator(xaTerminator)
                                                                   .withProtocolVersion(ProtocolVersion.VERSION_1_0)
                                                                   .withMaxBackoffMillils(instance.getMaxConnectionBackoffMillis())
                                                                   .build(), instance.getSize());
        }
    }

    private void startReverseInbound(ReverseInboundConnectionInformation connectionInformation, int numberOfInstances )
    {
        for(int i = 0; i < numberOfInstances; ++i)
        {
            Consumer<ReverseInboundServer> consumer = this::connected;
            Supplier<ReverseInboundServer> supplier = () -> {
               CompletableFuture<ReverseInboundServer> future = new CompletableFuture<>();
               AutoConnect.of(connectionInformation, future::complete,this, () -> workManager);
               return future.join();
            };
            Supplier<String> logMsg = () -> "casual reverse inbound connected to: " +
                    new InetSocketAddress(connectionInformation.getAddress().getHostName(), connectionInformation.getAddress().getPort());
            Work work = StartInboundServerWork.of(getInboundStartupServices(), logMsg, consumer, supplier);
            startWork(work, StartReverseInboundServerListener.of());
        }
    }

    private void startInboundServer( ConnectionInformation connectionInformation )
    {
        Consumer<CasualServer> consumer = (CasualServer runningServer) -> {
            server = runningServer;
            RuntimeInformation.setInboundStarted(true);
        };
        Supplier<CasualServer> supplier = () -> CasualServer.of(connectionInformation);
        Supplier<String> logMsg = () -> "Casual inbound server bound to port: " + connectionInformation.getPort();
        long delay = ConfigurationService.getInstance().getConfiguration().getInbound().getInitialDelay();
        Work work = StartInboundServerWork.of( getInboundStartupServices(), logMsg, consumer, supplier, delay);
        startWork(work, StartInboundServerListener.of());
    }

    private List<String> getInboundStartupServices()
    {
        return configurationService.getConfiguration().getInbound().getStartup().getServices();
    }

    private void startWork(Work work, WorkListener workListener)
    {
        try
        {
            workManager.startWork(work, WorkManager.INDEFINITE, null, workListener);
        }
        catch (WorkException e)
        {
            throw new InboundStartupException("Problem starting work", e);
        }
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory,
                                     ActivationSpec spec)
    {
        if( server != null )
        {
            server.close();
        }
        activations.remove(((CasualActivationSpec)spec).getPort() );
        log.finest(()->"endpointDeactivation()");
    }

    @Override
    public void start(BootstrapContext ctx)
            throws ResourceAdapterInternalException
    {
        log.finest(()->"start()");
        workManager = ctx.getWorkManager();
        xaTerminator = ctx.getXATerminator();
        JMXStartup.getInstance().initJMX();
    }

    @Override
    public void stop()
    {
        log.finest(()->"stop()");
    }

    //Return empty array not null. But specification says to return null if we don't support this feature, so ignoring.
    @SuppressWarnings("squid:S1168")
    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs)
            throws ResourceException
    {
        log.finest(()->"getXAResources()");
        return null;
    }

    public WorkManager getWorkManager()
    {
        return this.workManager;
    }

    public XATerminator getXATerminator()
    {
        return this.xaTerminator;
    }

    public Integer getInboundServerPort()
    {
        return  inboundServerPort;
    }

    public void setInboundServerPort( Integer port )
    {
        this.inboundServerPort = port;
    }

    public CasualServer getServer()
    {
        return server;
    }

    @Override
    public void disconnected(ReverseInboundServer server)
    {
        log.info(() -> "ReverseInbound: " + server.getAddress() + " disconnected");
        reverseInbounds.remove(server);
    }

    @Override
    public void connected(ReverseInboundServer server)
    {
        log.info(() -> "ReverseInbound: " + server.getAddress() + " connection resumed");
        reverseInbounds.add(server);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        CasualResourceAdapter that = (CasualResourceAdapter) o;
        return Objects.equals(activations, that.activations);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(activations);
    }

    @Override
    public String toString()
    {
        return "CasualResourceAdapter{" +
                "activations=" + activations +
                ", xaTerminator=" + xaTerminator +
                '}';
    }

}
