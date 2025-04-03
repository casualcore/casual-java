/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.exec;

import java.util.Objects;

public class ExecResult
{
    private final int exitCode;
    private final String output;

    private ExecResult( Builder builder )
    {
        this.exitCode = builder.exitCode;
        this.output = builder.output;
    }

    public int getExitCode()
    {
        return exitCode;
    }

    public String getOutput()
    {
        return output;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        ExecResult that = (ExecResult) o;
        return exitCode == that.exitCode && Objects.equals( output, that.output );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( exitCode, output );
    }

    @Override
    public String toString()
    {
        return "ExecResult{" +
                "exitCode=" + exitCode +
                ", output='" + output + '\'' +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( ExecResult src )
    {
        return new Builder()
                .exitCode( src.exitCode )
                .output( src.output );
    }

    public static final class Builder
    {
        private int exitCode = 0;
        private String output = "";

        private Builder()
        {
        }

        public Builder exitCode( int exitCode )
        {
            this.exitCode = exitCode;
            return this;
        }

        public Builder output( String output )
        {
            this.output = output;
            return this;
        }

        public ExecResult build()
        {
            return new ExecResult( this );
        }
    }
}
