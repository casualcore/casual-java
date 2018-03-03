/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is put on an EJB method to advertise to Casual that the
 * method should be callable from casual. This places restrictions on
 * the method:
 * <ul>
 *     <li>Shall be annotated as {@link javax.ejb.Remote}.</li>
 *     <li>{@link #name()} shall be unique throughout all deployments on the application server.</li>
 *     <li>Annotated classes FQCN shall be unique throughout all deployments on the application server.</li>
 * </ul>
 * The jndi name will be determined at runtime for calling the annotated method, though this can be overridden using
 * {@link CasualServiceJndiName}.
 *
 * @author jone, Chris Kelly
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CasualService
{
    /**
     * @return The name of the casual service
     */
    String name();

    String category() default "";
}
