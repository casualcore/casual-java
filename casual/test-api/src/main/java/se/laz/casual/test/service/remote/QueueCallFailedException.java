package se.laz.casual.test.service.remote;

public class QueueCallFailedException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public QueueCallFailedException()
    {
        super();
    }

    public QueueCallFailedException(String s)
    {
        super(s);
    }

    public QueueCallFailedException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public QueueCallFailedException(Throwable throwable)
    {
        super(throwable);
    }

    protected QueueCallFailedException(String s, Throwable throwable, boolean b, boolean b1)
    {
        super(s, throwable, b, b1);
    }
}
