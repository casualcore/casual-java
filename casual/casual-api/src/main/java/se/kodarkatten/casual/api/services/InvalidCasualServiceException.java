package se.kodarkatten.casual.api.services;

import se.kodarkatten.casual.api.CasualRuntimeException;

import java.lang.reflect.Method;

/**
 * Indicates that the CasualService annotation is placed on a method which cannot be
 * advertised as a casual service
 *
 * @author jone
 */
public final class InvalidCasualServiceException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;

    public InvalidCasualServiceException(CasualService service, Method method, Class<?> methodClass)
    {

    }
}