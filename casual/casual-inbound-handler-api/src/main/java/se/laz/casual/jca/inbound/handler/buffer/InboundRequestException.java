package se.laz.casual.jca.inbound.handler.buffer;

import se.laz.casual.api.CasualRuntimeException;

public class InboundRequestException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;

    public InboundRequestException(String message)
    {
        super(message);
    }
}
