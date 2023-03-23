package se.laz.casual.jca.inbound.handler.service.extension;

import se.laz.casual.api.CasualRuntimeException;

import java.util.function.Supplier;

public class ServiceHandlerExtensionMissingException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public ServiceHandlerExtensionMissingException(Supplier<String> message)
    {
        super(message.get());
    }
}
