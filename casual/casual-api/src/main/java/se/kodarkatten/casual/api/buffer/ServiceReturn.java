package se.kodarkatten.casual.api.buffer;

import se.kodarkatten.casual.api.flags.ErrorState;
import se.kodarkatten.casual.api.flags.ServiceReturnState;

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
