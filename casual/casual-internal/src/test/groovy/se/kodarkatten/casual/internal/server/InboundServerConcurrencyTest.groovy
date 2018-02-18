package se.kodarkatten.casual.internal.server

import se.kodarkatten.casual.network.protocol.io.LockableSocketChannel
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class InboundServerConcurrencyTest extends Specification
{
    @Shared AtomicInteger count = new AtomicInteger( 0 )

    @Shared List<Future> consumers = new ArrayList<>()
    @Shared int numberOfClients = 10
    @Shared ExecutorService executor = Executors.newSingleThreadExecutor()
    @Shared ExecutorService clientExecutor = Executors.newFixedThreadPool( numberOfClients )

    @Shared
    Consumer<LockableSocketChannel> closingConsumer = { s ->

        Future task = executor.submit( new Thread(){
            @Override
            void run()
            {
                assert(null != s)
                String read = readMsg( s.getSocketChannel() )
                sendMsg( s.getSocketChannel(), read )
                s.getSocketChannel().close()
                count.getAndIncrement()
            }
        } )
        consumers.add( task )
    }

    @Shared
    def okAddress = new InetSocketAddress(0)

    def "multiple concurrent clients with a single threaded server. All clients have own connection and socket channel so all can send message before server starts replies."()
    {
        setup:
        def msg = 'hello world'

        when:
        def server = InboundServer.of(okAddress, closingConsumer)
        def thread = new Thread( new Runnable() {
            @Override
            void run() {
                server.start()
            }
        })
        thread.start()

        List<Future> clientFutures = new ArrayList()
        for(final int i = 0; i < numberOfClients; ++i)
        {
            clientFutures.add( clientExecutor.submit( new Thread() {
                void run()
                {
                    SocketChannel client = connectTo(new InetSocketAddress(server.getPort()))
                    String m = msg + " " + currentThread().getName()
                    sendMsg(client, m )
                    String read = readMsg( client )
                    assert m == read
                    client.close()
                }
            } ) )
        }

        for( Future f: clientFutures )
        {
            f.get()
        }

        then:
        consumers.size() == numberOfClients
        for( Future t: consumers )
        {
            t.get()
        }
        count.get() == numberOfClients
    }

    def sendMsg(SocketChannel c, String msg)
    {
        ByteBuffer buffer = ByteBuffer.wrap(msg.bytes)
        c.write(buffer)
    }

    def readMsg(SocketChannel c )
    {
        ByteBuffer buffer = ByteBuffer.wrap( new byte[1024] )
        c.read( buffer )
        buffer.position()
        String s = new String( buffer.array(),0, buffer.position( ) )

        return s
    }

    SocketChannel connectTo(InetSocketAddress address)
    {
        SocketChannel channel = SocketChannel.open()
        channel.connect(address)
        return channel
    }
}
