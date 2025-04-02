/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.controller;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.Watch;
import se.laz.casual.test.k8s.TestKube;
import se.laz.casual.test.k8s.watchers.DeleteWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Controller responsible for provisioning and destruction of
 * resources within the TestKube.
 */
public class ProvisioningController
{
    private final List<Watch> watches = new ArrayList<>();
    private final List<DeleteWatcher<?>> deleteWatchers = new ArrayList<>();

    private final TestKube testKube;

    public ProvisioningController( TestKube testKube )
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
        for( Map.Entry<String, Pod> entry : testKube.getPods().entrySet() )
        {
            String name = entry.getKey();
            Pod p = entry.getValue();
            Map<String,String> labels = p.getMetadata().getLabels();
            labels.put( "TestKube", testKube.getLabel() );
            Pod updated = p.edit().editMetadata().withLabels( labels ).endMetadata().build();
            updated = testKube.getClient().pods().resource( updated ).serverSideApply();
            testKube.getResourcesStore().putPod( name, updated );
        }

        for( Map.Entry<String, Service> entry : testKube.getServices().entrySet() )
        {
            String name = entry.getKey();
            Service s = entry.getValue();
            Map<String,String> labels = s.getMetadata().getLabels();
            labels.put( "TestKube", testKube.getLabel() );
            Service updated = s.edit().editMetadata().withLabels( labels ).endMetadata().build();
            updated = testKube.getClient().services().resource( updated ).serverSideApply();
            testKube.getResourcesStore().putService( name, updated );
        }
    }

    public void waitUntilReady()
    {
        for( Pod p: testKube.getPods().values() )
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
            DeleteWatcher<Pod> podWatcher = new DeleteWatcher<>();
            Watch watch = testKube.getClient().pods().resource( p ).watch( podWatcher );

            watches.add( watch );
            deleteWatchers.add( podWatcher );

            testKube.getClient().pods().resource( p ).delete();
        }
        for( Service s: testKube.getClient().services().withLabel( "TestKube", testKube.getLabel() ).list().getItems() )
        {
            DeleteWatcher<Service> serviceWatcher = new DeleteWatcher<>();
            Watch watch = testKube.getClient().services().resource( s ).watch( serviceWatcher );

            watches.add( watch );
            deleteWatchers.add( serviceWatcher );

            testKube.getClient().services().resource( s ).delete();
        }
    }

    public void waitUntilDestroyed()
    {
        for( DeleteWatcher<?> deleteWatcher: deleteWatchers )
        {
            deleteWatcher.waitUntilDeleted();
        }
        for( Watch watch: watches )
        {
            watch.close();
        }
    }
}
