package se.kodarkatten.casual.api.services;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is put on an EJB method to advertise to Casual that the
 * method should be callable from casual. This places restrictions on
 * the method
 *
 * @author jone
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CasualService
{
    /**
     *
     * @return The name of the casual service
     */
    String value();
}
