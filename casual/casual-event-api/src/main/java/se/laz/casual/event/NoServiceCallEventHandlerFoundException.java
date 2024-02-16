package se.laz.casual.event;

import se.laz.casual.api.CasualRuntimeException;

public class NoServiceCallEventHandlerFoundException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public NoServiceCallEventHandlerFoundException(String message)
    {
        super(message);
    }
}
