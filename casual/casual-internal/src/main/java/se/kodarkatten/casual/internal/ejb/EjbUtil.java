package se.kodarkatten.casual.internal.ejb;

import se.kodarkatten.casual.api.services.CasualService;
import se.kodarkatten.casual.api.services.InvalidCasualServiceException;
import se.kodarkatten.casual.internal.cdi.CasualServiceDiscovery;

import javax.ejb.Remote;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by jone on 2017-03-03.
 */
public final class EjbUtil
{
    private final static Logger LOG = Logger.getLogger(EjbUtil.class.getName());


    public final static Class<?> getServiceRemoteInterface(CasualService service, Method serviceMethod, Class<?> serviceClass)
    {
        List<Class<?>> interfaces = Arrays.asList(serviceClass.getInterfaces());

        Optional<Class<?>> remoteInterface = interfaces.stream().filter(it ->
                        Arrays.asList(it.getAnnotations()).contains(Remote.class))
                           .findFirst();

        return null;
        //return remoteInterface.orElseThrow(InvalidCasualServiceException::new);
    }
}
