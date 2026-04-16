/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import se.laz.casual.api.buffer.type.CStringBuffer;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.network.CasualNWMessageDecoder;
import se.laz.casual.network.CasualNWMessageEncoder;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.inbound.ProtocolVersionValueHolder;
import se.laz.casual.network.outbound.Correlator;
import se.laz.casual.network.outbound.CorrelatorImpl;
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ServiceReplyNetworkBytesTests
{
    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include( ServiceReplyNetworkBytesTests.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }


    @State( Scope.Benchmark )
    public static class ExecutionPlan
    {
        public EmbeddedChannel fixChannel()
        {
//            NettyNetworkConnection instance;
//
            Correlator correlator = CorrelatorImpl.of();
//            ConversationMessageStorage conversationMessageStorage = ConversationMessageStorageImpl.of();
//            EmbeddedChannel channel;
//            NettyConnectionInformation ci = NettyConnectionInformation.createBuilder()
//                    .withAddress( new InetSocketAddress( 3712 ) )
//                    .withDomainId( UUID.randomUUID() )
//                    .withDomainName( "testDomain" )
//                    .withCorrelator( correlator )
//                    .build();
            ProtocolVersionValueHolder valueHolder = ProtocolVersionValueHolder.of();
            valueHolder.accept( ProtocolVersion.VERSION_1_4 );
//            var conversationMessageHandler = ConversationMessageHandler.of( conversationMessageStorage );
            return new EmbeddedChannel( CasualNWMessageDecoder.of( valueHolder ), CasualNWMessageEncoder.of() );

            //instance = new NettyNetworkConnection( ci, correlator, channel, conversationMessageStorage, () -> null, ErrorInformer.of( new CasualConnectionException( "fake" ) ) );
//            return channel;
        }

        EmbeddedChannel channel = fixChannel();


        UUID executionId = UUID.randomUUID();
        String payload = randomString( 1024 );
        ServiceBuffer serviceBuffer = ServiceBuffer.of( CStringBuffer.of( payload ) );

        CasualServiceCallReplyMessage message = createMessage();

        public CasualServiceCallReplyMessage createMessage( )
        {
            CasualServiceCallReplyMessage m = CasualServiceCallReplyMessage.createBuilder()
                    .setExecution( executionId )
                    .setError( ErrorState.OK )
                    .setProtocolVersion( ProtocolVersion.VERSION_1_4 )
                    .setServiceBuffer( serviceBuffer )
                    .setTransactionState( TransactionState.TX_ACTIVE )
                    .build();

            return m;
        }

        @Param( {
                "single",
                "multi-original",
                "multi-new"
        })
        public String mode;

        @Param( {
                //"toNetworkBytes",
                //"WriteableChannel",
                //"Unpooled.buffer()",
                "channel"
        } )
        public String test;

    }

    private static String randomString( int length )
    {
        byte[] array = new byte[length];
        Random random = new Random(42);
        random.nextBytes( array );
        return new String( array, StandardCharsets.UTF_8 );
    }

    @Benchmark
    @Fork( value = 1, warmups = 0 )
    @Warmup( iterations = 2 )
    @BenchmarkMode( {Mode.Throughput} )
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
            case "channel":
                doTestChannel( plan.message, plan.channel );
                break;
            default:
                throw new IllegalArgumentException( "Unsupported test: " + plan.test );
        }
    }

    private void setMode( CasualServiceCallReplyMessage message, String mode )
    {
        switch( mode )
        {
            case "single":
                message.setMode( CasualServiceCallReplyMessage.Mode.SINGLE );
                break;
            case "multi-original":
                message.setMode( CasualServiceCallReplyMessage.Mode.MULTI_ORIGINAL );
                break;
            case "multi-new":
                message.setMode( CasualServiceCallReplyMessage.Mode.MULTI_NEW );
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

    private void doTest( CasualServiceCallReplyMessage message )
    {
        ByteBuf buf = Unpooled.buffer();
        CasualNWMessageImpl<CasualServiceCallReplyMessage> msg = CasualNWMessageImpl.of(UUID.randomUUID(), message );
        CasualMessageEncoder.write(buf, msg );
        buf.release();
    }

    private void doTestChannel( CasualServiceCallReplyMessage message, EmbeddedChannel channel )
    {
        CasualNWMessageImpl<CasualServiceCallReplyMessage> msg = CasualNWMessageImpl.of(UUID.randomUUID(), message );

        channel.writeOneOutbound( msg );
        channel.flush();
        if( !channel.releaseOutbound() )
        {
            throw new RuntimeException( "message not received." );
        }
    }

    private void doTestWritableChannel( CasualServiceCallReplyMessage message )
    {
        OutputStream stream = new ByteArrayOutputStream( 1024 );
        WritableByteChannel sink = Channels.newChannel( stream );
        CasualNWMessageImpl<CasualServiceCallReplyMessage> msg = CasualNWMessageImpl.of(UUID.randomUUID(), message );
        CasualMessageEncoder.write(sink, msg );
    }

    private void doTestGetNetworkBytes( CasualServiceCallReplyMessage message )
    {
        CasualNWMessageImpl<CasualServiceCallReplyMessage> msg = CasualNWMessageImpl.of(UUID.randomUUID(), message );
        List<byte[]> data = msg.toNetworkBytes();
    }

}
