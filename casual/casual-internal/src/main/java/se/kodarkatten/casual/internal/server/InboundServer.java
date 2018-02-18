package se.kodarkatten.casual.internal.server;

import se.kodarkatten.casual.network.protocol.io.LockableSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Used for inbound connections from casual
 * Note that you should not do any actual work in the consumer
 * as that would block the servers thread
 * A consumer should also not throw, we do guard against it but please do not
 *
 * The server does not close itself except if accept throws, since then we are crashing
 * In general who ever owns an instance should close it
 *
 */
public final class InboundServer
{
    private static final Logger log = Logger.getLogger(InboundServer.class.getName());
    private final ServerSocketChannel ssc;
    private final Consumer<LockableSocketChannel> consumer;
    private AtomicBoolean running = new AtomicBoolean(true);
    private InboundServer(final ServerSocketChannel ssc, final Consumer<LockableSocketChannel> consumer)
    {
        this.ssc = ssc;
        this.consumer = consumer;
    }

    // "Resources should be closed"
    // this does not hold true in this case, this is a server and who ever owns this will close it
    // we also close in case any IOExceptions while blocking on accept - except when the server is going down
    // due to being stopped
    @SuppressWarnings("squid:S2095")
    public static InboundServer of(final InetSocketAddress address,  final Consumer<LockableSocketChannel> consumer)
    {
        Objects.requireNonNull(address, "address can not be null");
        Objects.requireNonNull(consumer, "consumer can not be null");
        ServerSocketChannel ssc;
        try
        {
            ssc = ServerSocketChannel.open();
            ssc.socket().bind(address);
        }
        catch (IOException e)
        {
            throw new InboundServerException("failed opening and binding channel", e);
        }
        return new InboundServer(ssc, consumer);
    }

    public static InboundServer of( int port, final Consumer<LockableSocketChannel> consumer )
    {
        InetSocketAddress address = new InetSocketAddress( port );
        return of( address, consumer );
    }

    public void start()
    {
        while(running.get())
        {
            LockableSocketChannel sc = null;
            try
            {
                sc = LockableSocketChannel.of( ssc.accept() );
            }
            catch (IOException e)
            {
                if(running.get())
                {
                    close();
                    throw new InboundServerException("failed accepting connection - server is crashing", e);
                }
                else
                {
                    log.info(() ->"accept threw but we are no longer running so this is fine since we are going down");
                }
            }
            try
            {
                if(null != sc)
                {
                    consumer.accept(sc);
                }
            }
            catch(Exception e)
            {
                // consumer did something stupid, should not crash the server though
                // we log it though
                log.warning(() -> "consumer accident: " + e);
            }
        }
        log.info(() -> "InboundServer going down");
        close();
    }

    public void stop()
    {
        try
        {
            this.running.set(false);
            this.ssc.close();
        } catch (IOException e)
        {
            log.warning( ()->"Close of inbound socket was not graceful." + e.getMessage() );
        }

    }

    public boolean running()
    {
        return running.get();
    }

    public int getPort()
    {
        return ssc.socket().getLocalPort();
    }

    private void close()
    {
        try
        {
            ssc.close();
        }
        catch (IOException e)
        {
            log.info(() -> "exception while closing channel: " + e);
        }
    }

}
