package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.network.connection.CasualConnectionException;
import se.kodarkatten.casual.network.io.CasualNetworkReader;
import se.kodarkatten.casual.network.io.CasualNetworkWriter;
import se.kodarkatten.casual.network.messages.CasualNWMessage;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * Created by aleph on 2017-06-14.
 */
public final class CasualNetworkConnection implements NetworkConnection
{
    private ManagedExecutorService executorService;
    private AsynchronousChannelGroup channelGroup;
    private AsynchronousSocketChannel casualServerConnection;

    private CasualNetworkConnection(final ManagedExecutorService executorService, final AsynchronousChannelGroup channelGroup, final AsynchronousSocketChannel casualServerConnection)
    {
        this.executorService = executorService;
        this.channelGroup = channelGroup;
        this.casualServerConnection = casualServerConnection;
    }

    public static CasualNetworkConnection of(final InetSocketAddress address)
    {
        Objects.requireNonNull(address, "address is not allowed to be null");
        ManagedExecutorService executorService = getManagedExecutorService();
        AsynchronousChannelGroup channelGroup = getChannelGroup(executorService);
        try
        {
            AsynchronousSocketChannel casualServerConnection = channelGroup.provider().openAsynchronousSocketChannel(channelGroup);
            casualServerConnection.connect(address).get();
            return new CasualNetworkConnection(executorService, channelGroup, casualServerConnection);
        }
        catch (IOException | InterruptedException | ExecutionException e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public final CasualNWMessage requestReply(CasualNWMessage message)
    {
        try
        {
            return executorService.submit(()->
            {
                CasualNetworkWriter.write(casualServerConnection, message);
                return CasualNetworkReader.read(casualServerConnection);
            }).get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public void close()
    {
        try
        {
            casualServerConnection.close();
        }
        catch (IOException e)
        {
            throw new CasualConnectionException(e);
        }
    }

    private static AsynchronousChannelGroup getChannelGroup(final ExecutorService executorService)
    {
        try
        {
            return AsynchronousChannelGroup.withThreadPool(executorService);
        }
        catch (IOException e)
        {
            throw new CasualConnectionException(e);
        }
    }

    private static ManagedExecutorService getManagedExecutorService()
    {
        try
        {
            InitialContext ctx = new InitialContext();
            return (ManagedExecutorService)ctx.lookup("java:comp/DefaultManagedExecutorService");
        }
        catch (NamingException e)
        {
            throw new CasualConnectionException(e);
        }
    }

}

