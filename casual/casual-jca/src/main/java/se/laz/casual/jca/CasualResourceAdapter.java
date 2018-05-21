/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import se.laz.casual.jca.inflow.CasualActivationSpec;
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
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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
public class CasualResourceAdapter implements ResourceAdapter, Serializable
{
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(CasualResourceAdapter.class.getName());
    private ConcurrentHashMap<Integer, CasualActivationSpec> activations;

    private WorkManager workManager;
    private XATerminator xaTerminator;

    private CasualServer server;

    @ConfigProperty( defaultValue = "7772")
    private Integer inboundServerPort;

    public Integer getInboundServerPort()
    {
        return  inboundServerPort;
    }

    public void setInboundServerPort( Integer port )
    {
        this.inboundServerPort = port;
    }

    public CasualResourceAdapter()
    {
        this.activations = new ConcurrentHashMap<>();
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
        log.info(()->"about to create casual inbound server");
        server = CasualServer.of(ci);
        log.info(()->"casual inbound server bound to port: " + ci.getPort() );
        activations.put( as.getPort(), as );
        log.finest(()->"end endpointActivation()");
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory,
                                     ActivationSpec spec)
    {
        server.close();
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
