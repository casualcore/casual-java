package se.kodarkatten.casual.internal.server

import java.nio.ByteBuffer
import spock.lang.Shared
import spock.lang.Specification

import java.nio.channels.SocketChannel
import java.util.concurrent.Semaphore
import java.util.function.Consumer

class InboundServerTest extends Specification
{
    @Shared
    Consumer<SocketChannel> closingConsumer = { s ->
        assert(null != s)
        print "got socket channel"
        s.close()
    }
    @Shared
    def okAddress = new InetSocketAddress(0)

    def "no address"()
    {
        when:
        def server = InboundServer.of(null, closingConsumer)
        then:
        server == null
        def e = thrown(NullPointerException)
        e.message == 'address can not be null'
    }

    def "no consumer"()
    {
        when:
        def server = InboundServer.of(okAddress, null)
        then:
        server == null
        def e = thrown(NullPointerException)
        e.message == 'consumer can not be null'
    }

    def "validate interaction"()
    {
        setup:
        def counter = 0
        def msg = 'hello world'
        def consumer = Mock(Consumer){
            10 * accept(_) >> {
                ++counter
            }
        }
        def server = InboundServer.of(okAddress, consumer)
        when:
        def thread = new Thread( new Runnable() {
            @Override
            void run() {
                server.start()
            }
        })
        thread.start()

        for(int i = 0; i < 10; ++i)
        {
            SocketChannel client = connectTo(new InetSocketAddress(server.getPort()))
            sendMsg(client, msg)
            client.close()
        }
        while(counter != 10)
        {}
        server.stop()
        then:
        server != null
        thread != null
        false == server.running()
        noExceptionThrown()
    }

    def "stupid consumer"()
    {
        setup:
        Semaphore sem = new Semaphore(1)
        def consumer = createStupidConsumer(sem)
        def server = InboundServer.of(okAddress, consumer)
        when:
        def thread = new Thread( new Runnable() {
            @Override
            void run() {
                server.start()
            }
        })
        thread.start()
        connectTo(new InetSocketAddress(server.getPort()))
        while(sem.availablePermits() > 0)
        {}
        server.stop()
        then:
        server != null
        false == server.running()
        noExceptionThrown()
    }

    def "validate one msg"()
    {
        setup:
        Semaphore sem = new Semaphore(1)
        def msg = 'hello world'
        def validatingConsumer = createValidatingConsumer(msg, sem)
        def server = InboundServer.of(okAddress, validatingConsumer)
        when:
        def thread = new Thread( new Runnable() {
            @Override
            void run() {
                server.start()
            }
        })
        thread.start()
        SocketChannel client = connectTo(new InetSocketAddress(server.getPort()))
        sendMsg(client, msg)
        while(sem.availablePermits() > 0)
        {}
        server.stop()
        then:
        server != null
        thread != null
        false == server.running()
        noExceptionThrown()
    }

    def "validate one msg - multiple clients"()
    {
        setup:
        Semaphore sem = new Semaphore(10)
        def msg = 'hello world'
        def validatingConsumer = createValidatingConsumer(msg, sem)
        def server = InboundServer.of(okAddress, validatingConsumer)
        when:
        def thread = new Thread( new Runnable() {
            @Override
            void run() {
                server.start()
            }
        })
        thread.start()
        for(int i = 0; i < 10 ; ++i)
        {
            SocketChannel client = connectTo(new InetSocketAddress(server.getPort()))
            sendMsg(client, msg)
        }
        while(sem.availablePermits() > 0)
        {}
        server.stop()
        then:
        server != null
        thread != null
        false == server.running()
        noExceptionThrown()
    }

    def sendMsg(SocketChannel c, String msg)
    {
        ByteBuffer buffer = ByteBuffer.wrap(msg.bytes)
        c.write(buffer)
    }

    SocketChannel connectTo(InetSocketAddress address)
    {
        SocketChannel channel = SocketChannel.open()
        channel.connect(address)
        return channel
    }

    def createValidatingConsumer(String msg, Semaphore sem)
    {
        byte[] msgBytes = msg.bytes
        ByteBuffer buffer = ByteBuffer.allocate(msgBytes.length)
        Consumer<SocketChannel> c = { s ->
            assert(null != s)
            new Thread({
                s.read(buffer)
                s.close()
                assert (Arrays.equals(msgBytes, buffer.array()))
                sem.acquire()
            }).start()
        }
        return c
    }

    def createStupidConsumer(Semaphore sem)
    {
        Consumer<SocketChannel> c = { s ->
            assert (null != s)
            new Thread({
                sem.acquire()
                throw new RuntimeException('ooopsie')
            }).start()
        }
        return c
    }
}
