package se.kodarkatten.casual.jca.inbound.handler.fielded;

import se.kodarkatten.casual.api.services.CasualService;

import java.lang.reflect.Method;

/**
 * Created by jone on 2017-02-27.
 */
public final class CasualServiceEntry
{
    private final CasualService service;

    private final Method serviceMethod;

    private Method proxyMethod;

    private final Class<?> serviceClass;

    public CasualServiceEntry(CasualService service, Method serviceMethod, Class<?> serviceClass)
    {
        this.service = service;
        this.serviceMethod = serviceMethod;
        this.serviceClass = serviceClass;
    }

    public CasualService getCasualService()
    {
        return this.service;
    }

    public Method getServiceMethod()
    {
        return (proxyMethod!=null)? this.proxyMethod : this.serviceMethod;
    }

    public Class<?> getServiceClass()
    {
        return this.serviceClass;
    }

    public Method getProxyMethod( )
    {
        return this.proxyMethod;
    }

    public void setProxyMethod( Method method )
    {
        this.proxyMethod = method;
    }
}