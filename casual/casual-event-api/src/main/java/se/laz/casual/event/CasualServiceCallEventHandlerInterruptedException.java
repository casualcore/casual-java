package se.laz.casual.event;

import se.laz.casual.api.CasualRuntimeException;

public class CasualServiceCallEventHandlerInterruptedException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CasualServiceCallEventHandlerInterruptedException(String message)
    {
        super(message);
    }
}
