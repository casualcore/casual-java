/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer;

import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.ServiceReturnState;

/**
 * @author jone
 */
public final class ServiceReturn<X extends CasualBuffer>
{
    private final X replyBuffer;

    private final ServiceReturnState serviceReturnState;

    private final ErrorState errorState;

    private final long userDefinedCode;

    public ServiceReturn(X replyBuffer, ServiceReturnState serviceReturnState, ErrorState errorState, long userDefinedCode)
    {
        this.replyBuffer = replyBuffer;
        this.serviceReturnState = serviceReturnState;
        this.errorState = errorState;
        this.userDefinedCode = userDefinedCode;
    }

    public X getReplyBuffer()
    {
        return replyBuffer;
    }

    public ServiceReturnState getServiceReturnState()
    {
        return serviceReturnState;
    }

    public ErrorState getErrorState()
    {
        return errorState;
    }

    public long getUserDefinedCode()
    {
        return userDefinedCode;
    }
}
