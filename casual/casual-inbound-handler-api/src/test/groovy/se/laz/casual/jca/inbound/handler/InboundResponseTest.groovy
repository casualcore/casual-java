/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler

import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.network.protocol.messages.service.ServiceBuffer
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class InboundResponseTest extends Specification
{

    @Shared InboundResponse instance
    @Shared InboundResponse specificInstance
    @Shared List<byte[]> payload = Arrays.asList( "buffer".getBytes( StandardCharsets.UTF_8 ) )
    @Shared ErrorState errorState = ErrorState.TPETRAN
    @Shared TransactionState transactionState = TransactionState.ROLLBACK_ONLY
    @Shared long userDefinedCode = 123L
    @Shared CasualBuffer buffer = ServiceBuffer.of( "test", payload )

    @Shared List<byte[]> payload2 = Arrays.asList( "payload2".getBytes( StandardCharsets.UTF_8 ) )
    @Shared List<byte[]> payload3 = Arrays.asList( "buffer".getBytes( StandardCharsets.UTF_8 ) )

    @Shared CasualBuffer buffer2 = ServiceBuffer.of( "test", payload2 )
    @Shared CasualBuffer buffer3 = ServiceBuffer.of( "test", payload3 )

    def setup()
    {
        instance = InboundResponse.createBuilder()
                .buffer( buffer )
                .build()
        specificInstance = InboundResponse.createBuilder()
                .buffer( buffer )
                .errorState( errorState )
                .transactionState( transactionState )
                .userSuppliedErrorCode( userDefinedCode )
                .build()
    }

    def cleanup()
    {
        instance = null
    }

    def "error state returns default OK value when none provided."()
    {
        expect:
        instance.getErrorState() == ErrorState.OK
    }

    def "error state returns provided."()
    {
        expect:
        specificInstance.getErrorState() == errorState
    }

    def "transaction state returns default TX_ACTIVE value when none provided."()
    {
        expect:
        instance.getTransactionState() == TransactionState.TX_ACTIVE
    }

    def "transaction state returns provided."()
    {
        expect:
        specificInstance.getTransactionState() == transactionState
    }

    def "user defined code returns default 0 value when none provided."()
    {
        expect:
        instance.getUserSuppliedErrorCode() == 0L
    }

    def "user defined code returns provided."()
    {
        expect:
        specificInstance.getUserSuppliedErrorCode() == userDefinedCode
    }

    def "get buffer returns provided value."()
    {
        expect:
        instance.getBuffer() == buffer
    }

    @Unroll
    def "equals and hashcode only allows referential equality."()
    {
        when:
        InboundResponse instance2 = InboundResponse.createBuilder().buffer( buf ).build()

        then:
        instance.equals( instance2 ) == result
        (instance.hashCode() == instance2.hashCode() ) == result

        where:
        buf   || result
        buffer  || false
        buffer3 || false
        buffer  || false
        buffer2 || false
        buffer2 || false
    }

    def "buffer null check throws NullPointer"()
    {
        when:
        InboundResponse.createBuilder().buffer( null )

        then:
        thrown NullPointerException.class
    }

    def "errorState null check throws NullPointer"()
    {
        when:
        InboundResponse.createBuilder().errorState( null )

        then:
        thrown NullPointerException.class
    }

    def "transactionState null check throws NullPointer"()
    {
        when:
        InboundResponse.createBuilder().transactionState( null )

        then:
        thrown NullPointerException.class
    }

    def "buffer null build throws IllegalStateException"()
    {
        when:
        InboundResponse.createBuilder().build();

        then:
        thrown IllegalStateException.class
    }

    def "equals self is true"()
    {
        expect:
        instance.equals( instance )
    }

    def "equals other type is false"()
    {
        expect:
        ! instance.equals( "123" )
    }

    def "equals null is false"()
    {
        expect:
        ! instance.equals( null )
    }

    def "toString returns value."()
    {
        expect:
        instance.toString() != null
    }
}
