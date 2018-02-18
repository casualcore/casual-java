package se.kodarkatten.casual.network.protocol.utils

import sun.reflect.generics.reflectiveObjects.NotImplementedException

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousByteChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.Future

/**
 * Created by aleph on 2017-03-16.
 */
/**
 * A LocalAsyncByteChannel that we can use when testing instead of actually going to the network
 */
class LocalAsyncByteChannel implements AsynchronousByteChannel
{
    List<ByteBuffer[]> bytes = new ArrayList<>()
    @Override
    <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler)
    {
        if(bytes.isEmpty() || 0 == dst.remaining())
        {
            handler.completed(0, attachment)
        }
        else
        {
            readBytes(dst)
            handler.completed(dst.array().length, attachment)
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
    Future<Integer> read(ByteBuffer dst)
    {
        throw new NotImplementedException()
    }

    @Override
    <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler)
    {
        int size = src.capacity()
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
