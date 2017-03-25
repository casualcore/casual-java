package se.kodarkatten.casual.api.xa;

/**
 * Created by aleph on 2017-03-14.
 */
public class XIDException extends RuntimeException
{
    private static final long serialVersionUID = 1l;
    public XIDException(String msg)
    {
        super(msg);
    }
}
