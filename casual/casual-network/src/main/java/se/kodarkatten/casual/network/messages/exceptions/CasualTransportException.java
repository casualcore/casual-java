package se.kodarkatten.casual.network.messages.exceptions;

import se.kodarkatten.casual.api.CasualRuntimeException;

/**
 * Created by aleph on 2017-02-23.
 */
public final class CasualTransportException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CasualTransportException(String msg)
    {
        super(msg);
    }
    public CasualTransportException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
