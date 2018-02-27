/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.jca.inflow.CasualActivationSpec;
import se.laz.casual.network.inbound.CasualServer;
import se.laz.casual.network.inbound.ConnectionInformation;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
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
        displayName = "Casual Resource Adapter",
        reauthenticationSupport = true,
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

    public CasualResourceAdapter()
    {
        this.activations = new ConcurrentHashMap<>();
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory,
                                   ActivationSpec spec) throws ResourceException
    {
        log.info("start endpointActivation() ");
        CasualActivationSpec as = (CasualActivationSpec) spec;
        ConnectionInformation ci = ConnectionInformation.createBuilder()
                                                        .withFactory(endpointFactory)
                                                        .withPort(as.getPort())
                                                        .withWorkManager(workManager)
                                                        .withXaTerminator(xaTerminator)
                                                        .build();
        log.info("about to create server endpointActivation()");
        server = CasualServer.of(ci);
        log.info("server created endpointActivation()");
        activations.put( as.getPort(), as );
        log.finest("endpointActivation()");
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory,
                                     ActivationSpec spec)
    {
        server.close();
        activations.remove(((CasualActivationSpec)spec).getPort() );
        log.finest("endpointDeactivation()");
    }

    @Override
    public void start(BootstrapContext ctx)
            throws ResourceAdapterInternalException
    {
        log.finest("start()");
        workManager = ctx.getWorkManager();
        xaTerminator = ctx.getXATerminator();
    }

    @Override
    public void stop()
    {
        log.finest("stop()");
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs)
            throws ResourceException
    {
        log.finest("getXAResources()");
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
