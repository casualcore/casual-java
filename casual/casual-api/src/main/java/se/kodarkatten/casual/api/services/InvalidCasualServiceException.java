package se.kodarkatten.casual.api.services;

import java.lang.reflect.Method;

/**
 * Indicates that the CasualService annotation is placed on a method which cannot be
 * advertised as a casual service
 *
 * @author jone
 */
public final class InvalidCasualServiceException extends RuntimeException
{
    public InvalidCasualServiceException(CasualService service, Method method, Class<?> methodClass)
    {

    }
}