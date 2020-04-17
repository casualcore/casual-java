package se.laz.casual.network.grpc.inbound;

import se.laz.casual.api.CasualRuntimeException;

public class CasualServerException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CasualServerException(String message, Throwable t)
    {
        super(message, t);
    }
}
