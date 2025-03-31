/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.Watch;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class KubeController
{
    private final List<Watch> watches = new ArrayList<>();
    private final List<PodWatcher> podWatchers = new ArrayList<>();
    private final List<KubeConnection> connections = new ArrayList<>();

    private final TestKube testKube;

    public KubeController( TestKube testKube )
    {
        this.testKube = testKube;
    }

    public void init()
    {
        initAsync();
        waitUntilReady();
    }

    public void initAsync()
    {
        for( Pod p: testKube.getPods() )
        {
            Map<String,String> labels = p.getMetadata().getLabels();
            labels.put( "TestKube", testKube.getLabel() );
            Pod updated = p.edit().editMetadata().withLabels( labels ).endMetadata().build();
            testKube.getClient().pods().resource( updated ).serverSideApply();
        }
    }

    public void waitUntilReady()
    {
        for( Pod p: testKube.getPods() )
        {
            testKube.getClient().pods().resource( p ).waitUntilReady( 1, TimeUnit.MINUTES );
        }
    }

    public void destroy()
    {
        destroyAsync();
        waitUntilDestroyed();
    }

    public void destroyAsync()
    {
        for( Pod p: testKube.getClient().pods().withLabel( "TestKube", testKube.getLabel() ).list().getItems() )
        {
            PodWatcher podWatcher = new PodWatcher();
            Watch watch = testKube.getClient().pods().resource( p ).watch( podWatcher );

            watches.add( watch );
            podWatchers.add( podWatcher );

            testKube.getClient().pods().resource( p ).delete();
        }
    }

    public void waitUntilDestroyed()
    {
        for( PodWatcher podWatcher: podWatchers )
        {
            podWatcher.waitUntilDeleted();
        }
        for( Watch watch: watches )
        {
            watch.close();
        }
    }

    public KubeConnection getConnection( String resource, int port )
    {
        if( !ContainerAwareness.inContainer() )
        {
            InetAddress local = InetAddress.getLoopbackAddress();
            LocalPortForward portForward = testKube.getClient().pods().withName( resource ).portForward( port, local, 0 );

            LocalConnection connection = new LocalConnection( portForward );
            connections.add( connection );

            return connection;
        }
        return null;
    }
}
