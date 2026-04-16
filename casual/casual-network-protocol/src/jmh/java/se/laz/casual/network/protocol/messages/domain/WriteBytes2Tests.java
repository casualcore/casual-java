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
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WriteBytes2Tests
{
    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include( WriteBytes2Tests.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }


    @State( Scope.Benchmark )
    public static class ExecutionPlan
    {
        @Param( {"1","2","4","8","16"} )
        int count;

        @Param( {"1024", "1024000", "10240000"} )
        int size;

        List<ByteBuffer> bytes;

        ByteBuf buf;

        @Setup
        public void setup()
        {
            bytes = new ArrayList<>(count);
            int chunk = size / count;
            for( int i=0;i< count; i++ )
            {
                bytes.add( ByteBuffer.wrap( randomBytes( chunk ) ) );
            }
        }

        @Setup( Level.Invocation )
        public void setupIteration()
        {
            buf = Unpooled.buffer( size, size );
        }

        @TearDown( Level.Invocation )
        public void tearDownIteration()
        {
            buf.release();
        }
    }

    private static byte[] randomBytes( int length )
    {
        byte[] array = new byte[length];
        Random random = new Random();
        random.nextBytes( array );
        return array;
    }

    private static String randomString( int length )
    {
        return new String( randomBytes( length ), StandardCharsets.UTF_8 );
    }


    @Benchmark
    @Fork( value = 1, warmups = 0 )
    @Warmup( iterations = 2 )
    //@Measurement( time = 1 )
    @BenchmarkMode( {Mode.Throughput} )
    public void singleImpl( ExecutionPlan plan )
    {
        for( ByteBuffer bytes: plan.bytes )
        {
            plan.buf.writeBytes( bytes );
        }
    }

}
