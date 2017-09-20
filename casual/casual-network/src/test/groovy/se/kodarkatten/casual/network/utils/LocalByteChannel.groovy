package se.kodarkatten.casual.network.utils

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
            return dst.array().length
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
