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

    public ServiceReturn(X replyBuffer, ServiceReturnState serviceReturnState, ErrorState errorState)
    {
        this.replyBuffer = replyBuffer;
        this.serviceReturnState = serviceReturnState;
        this.errorState = errorState;
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
}
