/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.utils

import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class LocalByteChannelTest extends Specification
{
    @Shared
    def payload = 'very nice payload'
    def "write/read"()
    {
        setup:
        byte[] data = payload.getBytes(StandardCharsets.UTF_8)
        ByteBuffer b = ByteBuffer.wrap(data)
        LocalByteChannel sink = new LocalByteChannel()
        ByteBuffer readBuffer = ByteBuffer.allocate(data.length)
        when:
        sink.write(b)
        sink.read(readBuffer)
        then:
        readBuffer.array() == data
        payload == new String(readBuffer.array(), StandardCharsets.UTF_8)
    }

    def "write/read with ByteUtils"()
    {
        setup:
        byte[] data = payload.getBytes(StandardCharsets.UTF_8)
        ByteBuffer b = ByteBuffer.wrap(data)
        LocalByteChannel sink = new LocalByteChannel()
        when:
        ByteUtils.writeFully(sink, b, data.length)
        ByteBuffer read = ByteUtils.readFully(sink, data.length)
        then:
        read.array() == data
        payload == new String(read.array(), StandardCharsets.UTF_8)
    }

    def "write/read casual message with network writer/reader"()
    {
        setup:
        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setDomainId(UUID.randomUUID())
                .setDomainName('Home sweet home')
                .setQueueNames(Arrays.asList("queueA1"))
                .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        LocalByteChannel sink = new LocalByteChannel()
        when:
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)
        then:
        msg == resurrectedMsg
    }

    def "readFully fail"()
    {
        setup:
        byte[] data = payload.getBytes(StandardCharsets.UTF_8)
        ByteBuffer b = ByteBuffer.wrap(data)
        LocalByteChannel sink = new LocalByteChannel()
        def badReadLength = data.length * 2
        when:
        ByteUtils.writeFully(sink, b, data.length)
        ByteUtils.readFully(sink, badReadLength)
        then:
        def e = thrown(CasualProtocolException)
        e.message == "expected to read: ${badReadLength} but could only read: ${data.length} bytes, broken pipe?"
    }

}
