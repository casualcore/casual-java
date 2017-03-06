package se.kodarkatten.casual.internal.cdi;

import se.kodarkatten.casual.api.services.CasualService;

import javax.ejb.Remote;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Created by jone on 2017-02-27.
 */
public class CasualServiceRegistry
{


    private final static Logger LOG = Logger.getLogger(CasualServiceRegistry.class.getName());

    private final static List<CasualServiceEntry> services = new CopyOnWriteArrayList<>();


    public void register(CasualService service, Method serviceMethod, Class<?> serviceClass)
    {
        CasualServiceEntry registryEntry = new CasualServiceEntry(service, serviceMethod, serviceClass);


        validateServiceEntry(registryEntry);


    }

    private void validateServiceEntry(CasualServiceEntry registryEntry)
    {

    }



}
