/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network

import io.netty.handler.codec.DecoderException
import se.laz.casual.api.CasualRuntimeException
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException
import spock.lang.Specification

class ExceptionToolTest extends Specification
{
    def 'findDecoderException - found when CasualDecoderException is in the middle of the cause chain'()
    {
        given:
        def protocolException = new CasualProtocolException('protocol error')
        def decoderException = new CasualDecoderException(protocolException, UUID.randomUUID())
        def wrapper = new CasualRuntimeException(decoderException)

        when:
        def result = ExceptionTool.findDecoderException(wrapper)

        then:
        result.isPresent()
        result.get() == decoderException
    }

    def 'findDecoderException - found when CasualDecoderException is the root and has a cause'()
    {
        given:
        def cause = new CasualRuntimeException('some cause')
        def decoderException = new CasualDecoderException(cause, UUID.randomUUID())

        when:
        def result = ExceptionTool.findDecoderException(decoderException)

        then:
        result.isPresent()
        result.get() == decoderException
    }

    def 'findDecoderException - found when deeply nested'()
    {
        given:
        def protocolException = new CasualProtocolException('protocol error')
        def decoderException = new CasualDecoderException(protocolException, UUID.randomUUID())
        def mid = new CasualRuntimeException(decoderException)
        def outer = new CasualRuntimeException(mid)

        when:
        def result = ExceptionTool.findDecoderException(outer)

        then:
        result.isPresent()
        result.get() == decoderException
    }

    def 'findDecoderException - empty when no CasualDecoderException in chain'()
    {
        given:
        def inner = new IllegalArgumentException('bad arg')
        def outer = new CasualRuntimeException(inner)

        when:
        def result = ExceptionTool.findDecoderException(outer)

        then:
        result.isEmpty()
    }

    def 'findDecoderException - empty when exception has no cause'()
    {
        given:
        def exception = new CasualRuntimeException('no cause')

        when:
        def result = ExceptionTool.findDecoderException(exception)

        then:
        result.isEmpty()
    }

    def 'findDecoderException - found when CasualDecoderException is the root with a cause'()
    {
        given:
        def protocolException = new CasualProtocolException('error')
        def decoderException = new CasualDecoderException(protocolException, UUID.randomUUID())

        when:
        def result = ExceptionTool.findDecoderException(decoderException)

        then:
        result.isPresent()
        result.get() == decoderException
    }

    def 'findProtocolException - found when CasualProtocolException is in the middle of the cause chain and has a cause'()
    {
        given:
        def root = new CasualRuntimeException('root cause')
        def protocolException = new CasualProtocolException('protocol error', root)
        def decoderException = new CasualDecoderException(protocolException, UUID.randomUUID())
        def wrapper = new CasualRuntimeException(decoderException)

        when:
        def result = ExceptionTool.findProtocolException(wrapper)

        then:
        result.isPresent()
        result.get() == protocolException
    }

    def 'findProtocolException - empty when no CasualProtocolException in chain'()
    {
        given:
        def inner = new IllegalArgumentException('bad arg')
        def outer = new CasualRuntimeException(inner)

        when:
        def result = ExceptionTool.findProtocolException(outer)

        then:
        result.isEmpty()
    }

    def 'findProtocolException - empty when exception has no cause'()
    {
        given:
        def exception = new CasualRuntimeException('no cause')

        when:
        def result = ExceptionTool.findProtocolException(exception)

        then:
        result.isEmpty()
    }

    def 'findProtocolException - found when CasualProtocolException is last in chain with no further cause'()
    {
        given:
        def protocolException = new CasualProtocolException('protocol error')
        def decoderException = new CasualDecoderException(protocolException, UUID.randomUUID())
        def wrapper = new CasualRuntimeException(decoderException)

        when:
        def result = ExceptionTool.findProtocolException(wrapper)

        then:
        result.isPresent()
        result.get() == protocolException
    }

    def 'findProtocolException - found when CasualProtocolException is root and has a cause'()
    {
        given:
        def cause = new CasualRuntimeException('underlying issue')
        def protocolException = new CasualProtocolException('protocol error', cause)

        when:
        def result = ExceptionTool.findProtocolException(protocolException)

        then:
        result.isPresent()
        result.get() == protocolException
    }

    def 'findProtocolException - found when wrapped in netty DecoderException'()
    {
        given:
        def protocolException = new CasualProtocolException('Message type 3100 is not supported by protocol version VERSION_1_4')
        def casualDecoderException = new CasualDecoderException(protocolException, UUID.randomUUID())
        def nettyDecoderException = new DecoderException(casualDecoderException)

        when:
        def result = ExceptionTool.findProtocolException(nettyDecoderException)

        then:
        result.isPresent()
        result.get() == protocolException
    }

    def 'findDecoderException - found when wrapped in netty DecoderException'()
    {
        given:
        def protocolException = new CasualProtocolException('Message type 3100 is not supported by protocol version VERSION_1_4')
        def casualDecoderException = new CasualDecoderException(protocolException, UUID.randomUUID())
        def nettyDecoderException = new DecoderException(casualDecoderException)

        when:
        def result = ExceptionTool.findDecoderException(nettyDecoderException)

        then:
        result.isPresent()
        result.get() == casualDecoderException
    }
}
