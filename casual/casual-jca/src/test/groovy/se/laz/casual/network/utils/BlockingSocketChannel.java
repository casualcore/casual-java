/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.utils;

import se.laz.casual.network.protocol.utils.AbstractTestSocketChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

public class BlockingSocketChannel extends AbstractTestSocketChannel
{
    private final CountDownLatch readLatch = new CountDownLatch( 1 );
    private final CountDownLatch blockingLatch;

    protected BlockingSocketChannel( CountDownLatch blockingLatch)
    {
        super( null );
        this.blockingLatch = blockingLatch;
    }

    public static BlockingSocketChannel of( CountDownLatch blockingLatch )
    {
        return new BlockingSocketChannel( blockingLatch );
    }

    public void awaitRead() throws InterruptedException
    {
        readLatch.await();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        readLatch.countDown();
        try
        {
            blockingLatch.await();
        } catch (InterruptedException e)
        {
        }

        return -1;
    }

}
