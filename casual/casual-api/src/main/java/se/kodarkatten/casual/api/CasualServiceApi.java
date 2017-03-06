package se.kodarkatten.casual.api;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.ServiceReturn;
import se.kodarkatten.casual.api.flags.Flag;

import java.util.concurrent.CompletableFuture;

/**
 * @author jone
 */
public interface CasualServiceApi
{
    <X extends CasualBuffer> ServiceReturn<X> tpcall(String serviceName, X data, Flag flags, Class<X> bufferClass);

    <X extends CasualBuffer> CompletableFuture<ServiceReturn<X>> tpacall(String serviceName, X data, Flag flags, Class<X> bufferClass);
}