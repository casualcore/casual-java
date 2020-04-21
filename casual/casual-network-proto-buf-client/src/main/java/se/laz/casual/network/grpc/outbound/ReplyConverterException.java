package se.laz.casual.network.grpc.outbound;

public class ReplyConverterException extends RuntimeException
{
    private final static long serialVersionUID = 1L;

    public ReplyConverterException(String message)
    {
        super(message);
    }
}
