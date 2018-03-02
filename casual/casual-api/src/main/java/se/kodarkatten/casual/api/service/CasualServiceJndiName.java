package se.kodarkatten.casual.api.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional annotation to be used if the a jndi name other than that determined at runtime
 * should be used when calling the {@link CasualService}.
 *
 * To be used on the EJB class which also contains {@link CasualService}
 * annotations on methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CasualServiceJndiName
{
    String value();
}
