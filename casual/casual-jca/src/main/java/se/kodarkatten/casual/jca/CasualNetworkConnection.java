package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.network.connection.CasualConnectionException;
import se.kodarkatten.casual.network.io.CasualNetworkReader;
import se.kodarkatten.casual.network.io.CasualNetworkWriter;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * Created by aleph on 2017-06-14.
 */
public final class CasualNetworkConnection implements NetworkConnection
{
    private SocketChannel socketChannel;
    private Object lock = new Object();

    private CasualNetworkConnection(final SocketChannel socketChannel)
    {
        this.socketChannel = socketChannel;
    }

    public static CasualNetworkConnection of(final InetSocketAddress address)
    {
        Objects.requireNonNull(address, "address is not allowed to be null");
        try
        {
            SocketChannel channel = SocketChannel.open();
            channel.connect(address);
            return new CasualNetworkConnection(channel);
        }
        catch (IOException e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public final <T extends CasualNetworkTransmittable,X extends CasualNetworkTransmittable>  CasualNWMessage<T> requestReply(CasualNWMessage<X> message)
    {
        synchronized (lock)
        {
            CasualNetworkWriter.write(socketChannel, message);
            return CasualNetworkReader.read(socketChannel);
        }
    }

    @Override
    public void close()
    {
        try
        {
            synchronized (lock)
            {
                socketChannel.close();
            }
        }
        catch (IOException e)
        {
            throw new CasualConnectionException(e);
        }
    }

}

