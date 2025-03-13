/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.Watch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Facade / Abstraction providing mechanisms to
 * provision kubernetes resources for the purpose of test.
 * <br/>
 * Resources should be created at the start of tests and destroyed at the end.
 * <br/>
 * The ability to interact with the k8s resources during the test are also provided
 * <ul>
 *     <li>check logs</li>
 *     <li>network connection details</li>
 *     <li>status, readiness</li>
 *     <li>modify / redeploy</li>
 *     <li>delete</li>
 *     <li>monitor</li>
 * </ul>
 * To allow maximum flexibility you can use the fabric8 api to define you resources.
 * These resources are then managed by the TestKube instance.
 * For more complex resource definitions it is advised to implement your own mapping
 * code which produces you own fabric8 {@link io.fabric8.kubernetes.client.dsl.Resource}
 * objects.
 *
 * By default, the TestKube instance will use a {@link io.fabric8.kubernetes.client.KubernetesClient}
 * with no additional configuration. If you require more complex setup, you can provide the appropriate
 * {@link io.fabric8.kubernetes.client.KubernetesClient} instance into the TestKube builder.
 *
 */
public class TestKube
{
    private final KubernetesClient client;
    private final String label;
    private final List<Pod> pods;

    private TestKube(final Builder builder )
    {
        this.client = builder.client;
        this.label = builder.label;
        this.pods = builder.pods;
    }

    public KubernetesClient getClient()
    {
        return client;
    }

    public String getLabel()
    {
        return label;
    }

    public List<Pod> getPods()
    {
        return new ArrayList<>( pods );
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        TestKube testKube = (TestKube) o;
        return Objects.equals( client, testKube.client ) && Objects.equals( label, testKube.label ) && Objects.equals( pods, testKube.pods );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( client, label, pods );
    }

    @Override
    public String toString()
    {
        return "TestKube{" +
                "client=" + client +
                ", label='" + label + '\'' +
                ", pods=" + pods +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    private final List<Watch> watches = new ArrayList<>();
    private final List<PodWatcher> podWatchers = new ArrayList<>();

    public void init()
    {
        initAsync();
        waitUntilReady();
    }

    public void initAsync()
    {
        for( Pod p: pods )
        {
            Map<String,String> labels = p.getMetadata().getLabels();
            labels.put( "TestKube", this.getLabel() );
            Pod updated = p.edit().editMetadata().withLabels( labels ).endMetadata().build();
            client.pods().resource( updated ).serverSideApply();
        }
    }

    public void waitUntilReady()
    {
        for( Pod p: pods )
        {
            client.pods().resource( p ).waitUntilReady( 1, TimeUnit.MINUTES );
        }
    }

    public void destroy()
    {
        destroyAsync();
        waitUntilDestroyed();
    }

    public void destroyAsync()
    {
        for( Pod p: client.pods().withLabel( "TestKube", this.getLabel() ).list().getItems() )
        {
            PodWatcher podWatcher = new PodWatcher();
            Watch watch = client.pods().resource( p ).watch( podWatcher );

            watches.add( watch );
            podWatchers.add( podWatcher );

            client.pods().resource( p ).delete();
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

    public static final class Builder
    {
        private KubernetesClient client;
        private String label = UUID.randomUUID().toString();
        private List<Pod> pods = new ArrayList<>();

        public Builder client( KubernetesClient client )
        {
            this.client = client;
            return this;
        }

        public Builder label( String label )
        {
            this.label = label;
            return this;
        }

        public Builder addPod( Pod pod )
        {
            this.pods.add( pod );
            return this;
        }

        public TestKube build()
        {
            if( this.client == null )
            {
                this.client = new KubernetesClientBuilder().build();
            }
            return new TestKube( this );
        }


    }
}
