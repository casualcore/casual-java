package se.kodarkatten.casual.network.utils

import sun.reflect.generics.reflectiveObjects.NotImplementedException

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousByteChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.Future

/**
 * Created by aleph on 2017-03-16.
 */
/**
 * A ByteSink that we can use when testing instead of actually going to the network
 */
class ByteSink implements AsynchronousByteChannel
{
    List<ByteBuffer[]> bytes = new ArrayList<>()
    @Override
    def <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler)
    {
        if(bytes.isEmpty() || 0 == dst.remaining())
        {
            handler.completed(0, attachment)
        }
        else
        {
            final byte[] bytes = readBytes(dst.remaining())
            dst.put(bytes)
            handler.completed(bytes.length, attachment)
        }
    }

    byte[] readBytes(int size)
    {
        ByteBuffer b = ByteBuffer.allocate(size)
        def iterator = bytes.iterator()
        int toRead = b.remaining()
        while(iterator.hasNext() && toRead > 0)
        {
            ByteBuffer c = iterator.next()
            if(c.remaining() > toRead)
            {
                byte[] dst = new byte[toRead]
                c.get(dst, 0, toRead)
            }
            else
            {
                b.put(c.array())
                iterator.remove()
            }
            toRead = b.remaining()
        }
        return b.wrap(b.array(), 0, b.position()).array()
    }

    @Override
    Future<Integer> read(ByteBuffer dst)
    {
        throw new NotImplementedException()
    }

    @Override
    <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler)
    {
        int size = src.position()
        src.flip()
        bytes.add(src)
        handler.completed(size, attachment)
    }

    @Override
    Future<Integer> write(ByteBuffer src)
    {
        throw new NotImplementedException()
    }

    @Override
    boolean isOpen()
    {
        return true
    }

    @Override
    void close() throws IOException
    {

    }
}
