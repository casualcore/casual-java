package se.kodarkatten.casual.api.xa;

import se.kodarkatten.casual.api.CasualRuntimeException;

/**
 * Created by aleph on 2017-03-14.
 */
public class XIDException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1l;
    public XIDException(String msg)
    {
        super(msg);
    }
}
