package se.kodarkatten.casual.api.buffer.type.fielded.marshalling;

import se.kodarkatten.casual.api.CasualRuntimeException;

public class FieldedUnmarshallingException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1;
    public FieldedUnmarshallingException(String msg)
    {
        super(msg);
    }
    public FieldedUnmarshallingException(Throwable t)
    {
        super(t);
    }
}
