/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;

import java.util.Objects;
import java.util.Optional;

public record ServiceCallResult(CasualNWMessage<CasualServiceCallReplyMessage> result, ErrorState resultCode)
{
    public ServiceCallResult
    {
        Objects.requireNonNull(resultCode, "resultCode can not be null");
    }

    public Optional<CasualNWMessage<CasualServiceCallReplyMessage>> maybeResult()
    {
        return Optional.of(result());
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private CasualNWMessage<CasualServiceCallReplyMessage> result;
        private ErrorState resultCode;
        public static Builder newBuilder()
        {
            return new Builder();
        }
        public Builder withResult(CasualNWMessage<CasualServiceCallReplyMessage> result)
        {
            this.result = result;
            return this;
        }
        public Builder withResultCode(ErrorState resultCode)
        {
            this.resultCode = resultCode;
            return this;
        }
        public ServiceCallResult build()
        {
            return new ServiceCallResult(result, resultCode);
        }
    }
}
