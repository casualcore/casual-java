package se.kodarkatten.casual.network.protocol.messages.exceptions;

import se.kodarkatten.casual.api.CasualRuntimeException;

/**
 * Created by aleph on 2017-02-23.
 */
public final class CasualProtocolException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CasualProtocolException(String msg)
    {
        super(msg);
    }
    public CasualProtocolException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
