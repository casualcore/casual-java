/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.controller;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.dsl.PodResource;
import se.laz.casual.test.k8s.TestKube;
import se.laz.casual.test.k8s.store.ResourceNotFoundException;

public class LogController
{
    private final TestKube testKube;

    public LogController( TestKube testKube )
    {
        this.testKube = testKube;
    }


    public String getLog( String pod )
    {
        PodResource resource = getPodResource( pod );

        return resource.getLog();
    }

    public String getLogTail( String pod, int lines )
    {
        PodResource resource = getPodResource( pod );

        return resource.tailingLines( lines ).getLog();
    }

    public String getLogSince( String pod, String sinceTime )
    {
        PodResource resource = getPodResource( pod );

        return resource.sinceTime( sinceTime ).getLog();
    }

    private PodResource getPodResource( String pod )
    {
        Pod p = null;

        if( this.testKube.getResourcesStore().getPods().containsKey( pod ) )
        {
            p = this.testKube.getResourcesStore().getPod( pod );
        }

        if( p == null )
        {
            p = this.testKube.getClient().pods().withName( pod ).get();
        }

        if( p == null )
        {
            throw new ResourceNotFoundException( "Unable to find pod " + pod );
        }

        return this.testKube.getClient().pods().resource( p );
    }
}
