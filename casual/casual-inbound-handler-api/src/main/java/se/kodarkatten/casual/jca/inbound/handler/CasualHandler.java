package se.kodarkatten.casual.jca.inbound.handler;

public interface CasualHandler
{
    /**
     * Determine if the handler know to handle a
     * service of this name.
     *
     * Differs from {@link #isServiceAvailable} in that a
     * service could be registered  but unavailable,
     * in which case this method would return true,
     * but {@link #isServiceAvailable} would return false.
     *
     * @param serviceName
     * @return
     */
    boolean canHandleService( String serviceName );

    /**
     * Determine if the provided service is available
     * in this handler.
     * @param serviceName name
     * @return
     */
    boolean isServiceAvailable( String serviceName );

    /**
     * Invoke the service and return the result of this invokation.
     * @param request received from client.
     * @return response to return to client.
     */
    InboundResponse invokeService( InboundRequest request );
}
