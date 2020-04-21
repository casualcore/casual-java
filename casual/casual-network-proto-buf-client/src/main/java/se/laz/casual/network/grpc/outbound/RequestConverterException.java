package se.laz.casual.network.grpc.outbound;

public class RequestConverterException extends RuntimeException
{
    private final static long serialVersionUID = 1L;
    public RequestConverterException(String message)
    {
        super(message);
    }
}
