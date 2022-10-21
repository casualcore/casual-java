package se.laz.casual.api.util;

import se.laz.casual.api.CasualRuntimeException;

public class CasualWorkException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CasualWorkException(String message, Throwable t)
    {
        super(message, t);
    }
}
