package se.kodarkatten.casual.network.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

/**
 * @author jone
 */
//Resource is managed else where
@SuppressWarnings("squid:S2095")
public final class CasualServer implements AutoCloseable
{
    private final AsynchronousServerSocketChannel server;

    public CasualServer()
    {
        try
        {
            server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(65000));
        }
        catch (IOException e)
        {
            throw new CasualStartupException(e);
        }
    }

    public final Future<AsynchronousSocketChannel> accept()
    {
        return server.accept();
    }

    @Override
    public void close() throws Exception
    {
        server.close();
    }
}
