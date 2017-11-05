package se.kodarkatten.casual.internal.server;

import se.kodarkatten.casual.api.CasualRuntimeException;

public final class InboundServerException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public InboundServerException(String message, Throwable t )
    {
        super(message, t);
    }
}
