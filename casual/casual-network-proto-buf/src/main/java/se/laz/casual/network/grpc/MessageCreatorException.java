package se.laz.casual.network.grpc;

import se.laz.casual.api.CasualRuntimeException;

public class MessageCreatorException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public MessageCreatorException(String s)
    {
        super(s);
    }
}
