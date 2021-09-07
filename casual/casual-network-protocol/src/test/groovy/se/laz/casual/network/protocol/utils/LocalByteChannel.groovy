/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.utils

import java.nio.ByteBuffer
import java.nio.channels.ByteChannel

class LocalByteChannel implements ByteChannel
{
    List<ByteBuffer[]> bytes = new ArrayList<>()

    @Override
    int read(ByteBuffer dst) throws IOException
    {
        if(bytes.isEmpty() || 0 == dst.remaining())
        {
            return -1
        }
        else
        {
            readBytes(dst)
            return dst.position()
        }
    }

    void readBytes(final ByteBuffer dst)
    {
        def iterator = bytes.iterator()
        while(iterator.hasNext() && dst.remaining() > 0)
        {
            ByteBuffer c = iterator.next()
            c.limit(c.capacity())
            int toRead = ((c.position() + dst.remaining()) > c.limit()) ? (c.limit() - c.position()) : dst.remaining()
            byte[] chunk = new byte[toRead]
            c.get(chunk, 0, toRead)
            dst.put(chunk)
            if(!c.hasRemaining())
            {
                iterator.remove()
            }
        }
    }

    @Override
    int write(ByteBuffer src) throws IOException
    {
        int size = src.capacity()
        src.flip()
        bytes.add(src)
        return size
    }

    @Override
    boolean isOpen()
    {
        return true
    }

    @Override
    void close() throws IOException
    {}
}
