/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import se.laz.casual.config.ConfigurationService;
import se.laz.casual.jca.inflow.CasualActivationSpec;
import se.laz.casual.jca.work.StartInboundServerListener;
import se.laz.casual.jca.work.StartInboundServerWork;
import se.laz.casual.network.inbound.CasualServer;
import se.laz.casual.network.inbound.ConnectionInformation;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
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
public class CasualResourceAdapter implements ResourceAdapter
{
    private static Logger log = Logger.getLogger(CasualResourceAdapter.class.getName());
    private ConcurrentHashMap<Integer, CasualActivationSpec> activations = new ConcurrentHashMap<>();

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
                .build();
        activations.put(as.getPort(), as);
        log.info(() -> "start casual inbound server" );
        startInboundServer( ci );
        log.finest(() -> "end endpointActivation()");

    }

    private void startInboundServer( ConnectionInformation connectionInformation )
    {
        Consumer<CasualServer> consumer = (CasualServer runningServer) -> server = runningServer;
        Work work = StartInboundServerWork.of( getInboundStartupServices(), connectionInformation, consumer);
        startWork(work);
    }

    private List<String> getInboundStartupServices()
    {
        return configurationService.getConfiguration().getInbound().getStartup().getServices();
    }

    private void startWork(Work work)
    {
        try
        {
            workManager.startWork(work, WorkManager.INDEFINITE, null, StartInboundServerListener.of());
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
