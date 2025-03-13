/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PodWatcher implements Watcher<Pod>
{
    private final CountDownLatch deleteLatch = new CountDownLatch( 1 );

    @Override
    public void eventReceived( Action action, Pod resource )
    {
        System.out.println( action.toString(  ) + ":" + resource.getStatus(  ).toString(  ) );
        switch( action )
        {
            case DELETED:
                System.out.println( "Deleted" );
                deleteLatch.countDown();
                break;
            case MODIFIED:
                System.out.println( "Modified" );
                break;
            case ADDED:
                System.out.println( "Added" );
                break;
        }
    }

    @Override
    public void onClose( WatcherException cause )
    {

    }

    public void waitUntilDeleted()
    {
        try
        {
            deleteLatch.await();
        }
        catch( InterruptedException e )
        {
            Thread.currentThread().interrupt();
            throw new RuntimeException( e );
        }
    }

    public boolean waitUntilDeleted( long timeout, TimeUnit unit )
    {
        try
        {
            return deleteLatch.await( timeout, unit );
        }
        catch( InterruptedException e )
        {
            Thread.currentThread().interrupt();
            throw new RuntimeException( e );
        }
    }
}
