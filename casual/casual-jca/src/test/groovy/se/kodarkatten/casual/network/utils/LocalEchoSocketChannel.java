package se.kodarkatten.casual.network.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class LocalEchoSocketChannel extends AbstractTestSocketChannel
{
    private boolean throwIOExceptionOnRead;
    private boolean throwRuntimeExceptionOnRead;
    private List<ByteBuffer> bytes = new ArrayList<>();
    private int throwOnNthRead;
    private int numberOfReads;
    private boolean throwInterruptedExceptionOnRead;

    public LocalEchoSocketChannel( )
    {
        super( null );
    }

    public static LocalEchoSocketChannel of()
    {
        return new LocalEchoSocketChannel();
    }

    public void setThrowIOExceptionOnRead()
    {
        throwIOExceptionOnRead = true;
    }

    public void setThrowInterruptedExceptionOnRead()
    {
        throwInterruptedExceptionOnRead = true;
    }

    public void setThrowRuntimeExceptionOnRead()
    {
        setThrowRuntimeExceptionOnNthRead(1);
    }

    public void setThrowRuntimeExceptionOnNthRead(int n)
    {
        throwOnNthRead = n;
        throwRuntimeExceptionOnRead = true;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        if(throwIOExceptionOnRead)
        {
            throw new IOException();
        }
        if(throwInterruptedExceptionOnRead)
        {
            Thread.currentThread().interrupt();
            return -1;
        }
        if(throwRuntimeExceptionOnRead && (numberOfReads+1) == throwOnNthRead)
        {
            ++numberOfReads;
            throw new RuntimeException();
        }
        if(bytes.isEmpty() || 0 == dst.remaining())
        {
            return -1;
        }
        else
        {
            readBytes(dst);
            ++numberOfReads;
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

    public void dump()
    {
        bytes.stream()
             .forEach(b -> System.out.println(Arrays.toString(b.array())));
    }

}
