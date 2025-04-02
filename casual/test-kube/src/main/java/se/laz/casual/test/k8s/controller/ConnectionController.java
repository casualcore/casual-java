/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.controller;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.PortForwardable;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import se.laz.casual.test.k8s.TestKube;
import se.laz.casual.test.k8s.connection.ConnectionException;
import se.laz.casual.test.k8s.connection.KubeConnection;
import se.laz.casual.test.k8s.connection.PortForwardedConnection;
import se.laz.casual.test.k8s.connection.ServiceConnection;
import se.laz.casual.test.k8s.runtime.ContainerAwareness;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Controller responsible for handling connection requests to resources in the TestKube.
 */
public class ConnectionController
{
    Logger log = Logger.getLogger( ConnectionController.class.getName());

    private final List<KubeConnection> connections = new ArrayList<>();

    private final TestKube testKube;

    public ConnectionController( TestKube testKube )
    {
        this.testKube = testKube;
    }

    public KubeConnection getConnection( String resource, int targetPort )
    {
        // Check for service in store.
        Service s = null;
        if( this.testKube.getResourcesStore().getServices().containsKey( resource ) )
        {
            Service service = this.testKube.getResourcesStore().getServices().get( resource );
            s = this.testKube.getClient().services().resource( service ).get();
        }

        // Check for service in cluster.
        if( s == null )
        {
            s = this.testKube.getClient().services().withName( resource ).get();
        }

        // No match for resource.
        if( s == null )
        {
            throw new ConnectionException( "Resource unavailable." );
        }

        // Check if client can connect to the service.
        if( canConnect( s.getMetadata().getName(), targetPort ) )
        {
            return new ServiceConnection( s.getMetadata().getName(), targetPort );
        }

        // Fix for running from outside a container / cluster.
        if( !ContainerAwareness.inContainer() )
        {
            log.info( ()->"Running outside of a container, attempting to connect externally." );
            // Check if the service should be externally accessible.
            if( s.getSpec().getType().equals( "LoadBalancer" ) )
            {
                // Attempt to access service externally.
                String externalIp = s.getStatus().getLoadBalancer().getIngress().get( 0 ).getIp();
                int externalPort = s.getSpec().getPorts().stream()
                        .filter( p -> p.getTargetPort().getIntVal() == targetPort )
                        .findFirst()
                        .map( ServicePort::getPort )
                        .orElse( -1 );
                log.info( ()-> "External IP: " + externalIp + ". External Port: " + externalPort );
                if( !( externalPort == -1 ) )
                {
                    if( canConnect( externalIp, externalPort ) )
                    {
                        log.info( ()->"Connection available externally." );
                        return new ServiceConnection( externalIp, externalPort );
                    }
                }
            }

            log.warning( ()-> "Creating a port forward connection for service: "+ resource + ", to allow seamless connectivity during development. " +
                    "Load Balancing will not work. Do NOT use for performance testing." );
            return getPortForwardConnection( resource, targetPort );
        }

        throw new ConnectionException( "Unable to connect to service: " + resource );
    }

    private boolean canConnect( String host, int port )
    {
        try( Socket socket = new Socket() )
        {
            socket.connect( new InetSocketAddress( host, port ), 500 );
            return true;
        }
        catch( IOException e )
        {
            log.finest( "Unable to connect to service externally." );
        }
        return false;
    }


    public KubeConnection getPortForwardConnection( String resource, int port )
    {
        // Check for service in store.
        if( this.testKube.getResourcesStore().getServices().containsKey( resource ) )
        {
            Service service = this.testKube.getResourcesStore().getServices().get( resource );
            return createPortForwardConnection( testKube.getClient().services().resource( service ), port );
        }

        // Check for service in cluster.
        ServiceResource<Service> s = this.testKube.getClient().services().withName( resource );
        if( s.get() != null )
        {
            return createPortForwardConnection( s, port );
        }

        // Check for pod in store.
        if( this.testKube.getResourcesStore().getPods().containsKey( resource ) )
        {
            Pod pod = this.testKube.getResourcesStore().getPods().get( resource );
            return createPortForwardConnection( testKube.getClient().pods().resource( pod ), port );
        }

        // Check for pod in cluster.
        PodResource p = this.testKube.getClient().pods().withName( resource );
        if( p.get() != null )
        {
            return createPortForwardConnection( p, port );
        }

        // No match for resource.
        throw new ConnectionException( "Resource unavailable." );
    }

    private KubeConnection createPortForwardConnection( PortForwardable resource, int port )
    {
        LocalPortForward portForward = createPortForward( resource, port );

        PortForwardedConnection connection = new PortForwardedConnection( portForward );
        connections.add( connection );

        return connection;
    }

    private LocalPortForward createPortForward( PortForwardable resource, int port )
    {
        InetAddress local = InetAddress.getLoopbackAddress();
        return resource.portForward( port, local, 0 );
    }
}
