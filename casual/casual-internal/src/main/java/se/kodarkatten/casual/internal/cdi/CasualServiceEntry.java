package se.kodarkatten.casual.internal.cdi;

import se.kodarkatten.casual.api.services.CasualService;

import java.lang.reflect.Method;

/**
 * Created by jone on 2017-02-27.
 */
public final class CasualServiceEntry
{
    private final CasualService service;

    private final Method serviceMethod;

    private final Class<?> serviceClass;

    public CasualServiceEntry(CasualService service, Method serviceMethod, Class<?> serviceClass)
    {
        this.service = service;
        this.serviceMethod = serviceMethod;
        this.serviceClass = serviceClass;
    }
}