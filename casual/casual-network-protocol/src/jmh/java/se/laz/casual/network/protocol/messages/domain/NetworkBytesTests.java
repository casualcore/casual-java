/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.messages.domain.TransactionType;
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class NetworkBytesTests
{
    @State( Scope.Benchmark )
    public static class ExecutionPlan
    {
        UUID executionId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();
        String domainName = "my test domain";

        List<String> serviceNames = Arrays.asList( "testService", "anotherService" );
        List<String> queueNames = Arrays.asList( "testQueue", "anotherQueue" );

        List<Service> services = serviceNames.stream().map( it -> Service.of( it, "test", TransactionType.AUTOMATIC ) ).toList();
        List<Queue> queues = queueNames.stream().map( it -> Queue.createBuilder().withName( it ).withProtocolVersion( ProtocolVersion.VERSION_1_4 ).build() ).toList();

        CasualDomainDiscoveryReplyMessage message = createMessage();

        public CasualDomainDiscoveryReplyMessage createMessage( )
        {
            CasualDomainDiscoveryReplyMessage m = CasualDomainDiscoveryReplyMessage.of( executionId, domainId, domainName, ProtocolVersion.VERSION_1_4 );
            m.setServices( services );
            m.setQueues( queues );

            return m;
        }

        @Param( {"single","multi-original","multi-new"})
        public String mode;

        @Param( {"toNetworkBytes","WriteableChannel","Unpooled.buffer()"} )
        public String test;

    }

    @Benchmark
    @Fork( value = 1, warmups = 0 )
    @Warmup( iterations = 2 )
    @BenchmarkMode( {Mode.AverageTime, Mode.Throughput} )
    public void singleImpl( ExecutionPlan plan )
    {
        setMode( plan.message, plan.mode );

        switch( plan.test )
        {
            case "toNetworkBytes":
                doTestGetNetworkBytes( plan.message );
                break;
            case "WriteableChannel":
                doTestWritableChannel( plan.message );
                break;
            case "Unpooled.buffer()":
                doTest( plan.message );
                break;
            default:
                throw new IllegalArgumentException( "Unsupported test: " + plan.test );
        }
    }

    private void setMode( CasualDomainDiscoveryReplyMessage message, String mode )
    {
        switch( mode )
        {
            case "single":
                message.setMode( CasualDomainDiscoveryReplyMessage.Mode.SINGLE );
                break;
            case "multi-original":
                message.setMode( CasualDomainDiscoveryReplyMessage.Mode.MULTI_ORIGINAL );
                break;
            case "multi-new":
                message.setMode( CasualDomainDiscoveryReplyMessage.Mode.MULTI_NEW );
                break;
            default:
                throw new IllegalArgumentException( "Unsupported mode: " + mode);
        }
    }


//    @Benchmark
//    @Fork( value = 1, warmups = 0 )
//    @Warmup( iterations = 2 )
//    @BenchmarkMode( {Mode.AverageTime, Mode.Throughput} )
//    public void mulitpleOrginal( ExecutionPlan plan )
//    {
//        plan.message.setMode( CasualDomainDiscoveryReplyMessage.Mode.MULTI_ORIGINAL );
//        doTest( plan.message );
//    }
//
//    @Benchmark
//    @Fork( value = 1, warmups = 0 )
//    @Warmup( iterations = 2 )
//    @BenchmarkMode( {Mode.AverageTime, Mode.Throughput} )
//    public void multipleNew( ExecutionPlan plan )
//    {
//        plan.message.setMode( CasualDomainDiscoveryReplyMessage.Mode.MULTI_NEW );
//        doTest( plan.message );
//    }

    private void doTest( CasualDomainDiscoveryReplyMessage message )
    {
        ByteBuf buf = Unpooled.buffer();
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> msg = CasualNWMessageImpl.of(UUID.randomUUID(), message );
        CasualMessageEncoder.write(buf, msg );
        buf.release();
    }

    private void doTestWritableChannel( CasualDomainDiscoveryReplyMessage message )
    {
        OutputStream stream = new ByteArrayOutputStream( 1024 );
        WritableByteChannel sink = Channels.newChannel( stream );
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> msg = CasualNWMessageImpl.of(UUID.randomUUID(), message );
        CasualMessageEncoder.write(sink, msg );
    }

    private void doTestGetNetworkBytes( CasualDomainDiscoveryReplyMessage message )
    {
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> msg = CasualNWMessageImpl.of(UUID.randomUUID(), message );
        List<byte[]> data = msg.toNetworkBytes();
    }

}
