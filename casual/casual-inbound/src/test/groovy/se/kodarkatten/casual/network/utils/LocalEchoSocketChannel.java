package se.kodarkatten.casual.network.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocalEchoSocketChannel extends AbstractTestSocketChannel
{
    List<ByteBuffer> bytes = new ArrayList<>();
    AtomicBoolean connected = new AtomicBoolean( true );

    public LocalEchoSocketChannel( )
    {
        super( null );
    }

    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        if(bytes.isEmpty() || 0 == dst.remaining())
        {
            return -1;
        }
        else
        {
            readBytes(dst);
            return dst.array().length;
        }
    }

    void readBytes(final ByteBuffer dst)
    {
        Iterator<ByteBuffer> iterator = bytes.iterator();
        while(iterator.hasNext() && dst.remaining() > 0)
        {
            ByteBuffer c = iterator.next();
            c.limit(c.capacity());
            int toRead = ((c.position() + dst.remaining()) > c.limit()) ? (c.limit() - c.position()) : dst.remaining();
            byte[] chunk = new byte[toRead];
            c.get(chunk, 0, toRead);
            dst.put(chunk);
            if(!c.hasRemaining())
            {
                iterator.remove();
            }
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        int size = src.capacity();
        src.flip();
        bytes.add(src);
        return size;
    }

    @Override
    public boolean isConnected()
    {
        return connected.get();
    }

    public void disconnect()
    {
        connected.set( false );
    }


}
