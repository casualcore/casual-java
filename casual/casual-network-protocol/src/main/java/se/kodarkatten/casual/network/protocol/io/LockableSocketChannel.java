package se.kodarkatten.casual.network.protocol.io;

import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wrapper class to allow concurrent Read and Write
 * access to a {@link SocketChannel}.
 *
 * This is back by two {@link ReentrantLock}s for Read and Write.
 *
 * Prior to using the {@link SocketChannel} a call to
 * {@link #lockRead} or {@link #lockWrite} should be called.
 * Following the operations the {@link #unlockRead} or{@link #unlockWrite} methods
 * should be called in a finally block.
 */
public class LockableSocketChannel
{
    private final SocketChannel channel;
    private final Lock readLock = new ReentrantLock( true );
    private final Lock writeLock = new ReentrantLock( true );
    private final AtomicInteger lockedRead = new AtomicInteger( 0 );
    private final AtomicInteger lockedWrite = new AtomicInteger( 0 );

    private LockableSocketChannel(SocketChannel channel)
    {
        this.channel = channel;
    }

    public static LockableSocketChannel of(SocketChannel channel )
    {
        Objects.requireNonNull( channel, "SocketChannel is null." );
        return new LockableSocketChannel( channel );
    }

    /**
     * Retrieve the {@link SocketChannel}.
     *
     * Prior to using the {@link SocketChannel} a call to
     * {@link #lockRead} or {@link #lockWrite} should be called.
     *
     * @return SocketChannel
     */
    public SocketChannel getSocketChannel()
    {
        return this.channel;
    }

    /**
     * Acquire a write lock.
     *
     * If the lock is not available then the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until the lock has been acquired.
     */
    public void lockWrite()
    {
        writeLock.lock();
        lockedWrite.incrementAndGet();
    }

    /**
     * Acquire a read lock.
     *
     * If the lock is not available then the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until the lock has been acquired.
     */
    public void lockRead()
    {
        readLock.lock();
        lockedRead.incrementAndGet();
    }

    public boolean isReadLocked()
    {
        return lockedRead.get() > 0;
    }

    public boolean isWriteLocked()
    {
        return lockedWrite.get() > 0;
    }

    /**
     * Releases the write lock.
     */
    public void unlockWrite()
    {
        writeLock.unlock();
        lockedWrite.decrementAndGet();
    }

    /**
     * Releases the read lock.
     */
    public void unlockRead()
    {
        readLock.unlock();
        lockedRead.decrementAndGet();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        LockableSocketChannel that = (LockableSocketChannel) o;
        return Objects.equals(channel, that.channel) &&
                Objects.equals(readLock, that.readLock) &&
                Objects.equals(writeLock, that.writeLock) &&
                Objects.equals(lockedRead, that.lockedRead) &&
                Objects.equals(lockedWrite, that.lockedWrite);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(channel, readLock, writeLock, lockedRead, lockedWrite);
    }

    @Override
    public String toString()
    {
        return "LockableSocketChannel{" +
                "channel=" + channel +
                ", readLock=" + readLock +
                ", writeLock=" + writeLock +
                ", lockedRead=" + lockedRead +
                ", lockedWrite=" + lockedWrite +
                '}';
    }
}
