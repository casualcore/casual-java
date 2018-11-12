/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;

import java.io.Serializable;
import java.util.Objects;

public class InboundResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final CasualBuffer buffer;
    private final ErrorState errorState;
    private final TransactionState transactionState;
    private final long userSuppliedErrorCode;

    private InboundResponse( CasualBuffer buffer, ErrorState errorState, TransactionState transactionState, long userSuppliedErrorCode)
    {
        this.buffer = buffer;
        this.errorState = errorState;
        this.transactionState = transactionState;
        this.userSuppliedErrorCode = userSuppliedErrorCode;
    }

    public CasualBuffer getBuffer( )
    {
        return this.buffer;
    }

    public ErrorState getErrorState()
    {
        return this.errorState;
    }

    public TransactionState getTransactionState()
    {
        return this.transactionState;
    }

    public long getUserSuppliedErrorCode()
    {
        return this.userSuppliedErrorCode;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private CasualBuffer buffer;
        private ErrorState errorState = ErrorState.OK;
        private TransactionState transactionState = TransactionState.TX_ACTIVE;
        private long userSuppliedErrorCode = 0L;

        public Builder buffer( CasualBuffer buffer )
        {
            Objects.requireNonNull( buffer,"Buffer cannot be null." );
            this.buffer = buffer;
            return this;
        }

        public Builder errorState( ErrorState errorState )
        {
            Objects.requireNonNull( errorState, "Error state cannot be null." );
            this.errorState = errorState;
            return this;
        }

        public Builder transactionState( TransactionState transactionState )
        {
            Objects.requireNonNull( transactionState, "Transaction State cannot be null." );
            this.transactionState = transactionState;
            return this;
        }

        public Builder userSuppliedErrorCode(long userSuppliedErrorCode )
        {
            this.userSuppliedErrorCode = userSuppliedErrorCode;
            return this;
        }

        public InboundResponse build()
        {
            if( buffer == null )
            {
                throw new IllegalStateException( "Buffer must be set before you can build." );
            }
            return new InboundResponse( buffer,errorState,transactionState, userSuppliedErrorCode);
        }
    }

    @Override
    public String toString()
    {
        return "InboundResponse{" +
                "buffer=" + buffer +
                ", errorState=" + errorState +
                ", transactionState=" + transactionState +
                ", userSuppliedErrorCode=" + userSuppliedErrorCode +
                '}';
    }
}
