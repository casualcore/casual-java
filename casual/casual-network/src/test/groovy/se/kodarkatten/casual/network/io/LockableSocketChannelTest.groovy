package se.kodarkatten.casual.network.io

import se.kodarkatten.casual.network.io.LockableSocketChannel
import spock.lang.Shared
import spock.lang.Specification

import java.nio.channels.SocketChannel

class LockableSocketChannelTest extends Specification
{

    @Shared LockableSocketChannel instance
    @Shared SocketChannel socketChannel

    def setup()
    {
        socketChannel = Mock( SocketChannel )
        instance = LockableSocketChannel.of( socketChannel )
    }

    def "Get Socket Channel."()
    {
        expect:
        instance.getSocketChannel() == socketChannel
    }

    def "Of null throws null pointer exception."()
    {
        when:
        LockableSocketChannel.of( null )

        then:
        thrown NullPointerException.class
    }

    def "Get write lock is then locked for write."()
    {
        when:
        instance.lockWrite()

        then:
        instance.isWriteLocked()
        ! instance.isReadLocked()
    }

    def "Get read lock is then locked for read."()
    {
        when:
        instance.lockRead()

        then:
        instance.isReadLocked()
        ! instance.isWriteLocked()
    }

    def "Get write lock then unlock is no longer locked for write."()
    {
        given:
        instance.lockWrite()

        when:
        instance.unlockWrite()

        then:
        ! instance.isWriteLocked()
    }

    def "Get read lock then unlock is no longer locked for read."()
    {
        given:
        instance.lockRead()

        when:
        instance.unlockRead()

        then:
        ! instance.isReadLocked()
    }

    def "Multiple reentrant read locks, followed by same number of unlocks. Lock remains until final unlock."()
    {
        given:
        instance.lockRead()
        instance.lockRead()

        when:
        instance.unlockRead()

        then:
        instance.isReadLocked()

        when:
        instance.unlockRead()

        then:
        ! instance.isReadLocked()
    }

    def "Multiple reentrant read locks, followed by too many of unlocks throws IllegalMonitorStateException."()
    {
        given:
        instance.lockRead()
        instance.lockRead()
        instance.unlockRead()
        instance.unlockRead()

        when:
        instance.unlockRead()

        then:
        thrown IllegalMonitorStateException.class
    }

    def "Multiple reentrant write locks, followed by same number of unlocks. Lock remains until final unlock."()
    {
        given:
        instance.lockWrite()
        instance.lockWrite()

        when:
        instance.unlockWrite()

        then:
        instance.isWriteLocked()

        when:
        instance.unlockWrite()

        then:
        ! instance.isWriteLocked()
    }

    def "Multiple reentrant write locks, followed by too many of unlocks throws IllegalMonitorStateException."()
    {
        given:
        instance.lockWrite()
        instance.lockWrite()
        instance.unlockWrite()
        instance.unlockWrite()

        when:
        instance.unlockWrite()

        then:
        thrown IllegalMonitorStateException.class
    }
}
