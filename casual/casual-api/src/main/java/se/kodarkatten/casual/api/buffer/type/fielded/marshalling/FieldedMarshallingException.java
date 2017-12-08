package se.kodarkatten.casual.api.buffer.type.fielded.marshalling;

import se.kodarkatten.casual.api.CasualRuntimeException;

public class FieldedMarshallingException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1;
    public FieldedMarshallingException(String msg)
    {
        super(msg);
    }
    public FieldedMarshallingException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
