/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

public final class TransactionAttributeTypeFinder
{
    private TransactionAttributeTypeFinder()
    {

    }

    /**
     * Check the {@link CasualServiceMetaData} method and service class
     * annotations to determine which is applicable based on information
     * provided in {@link TransactionAttribute}
     * @param entry for which to find the appropriate Transaction information for.
     * @return the determined transaction type.
     */
    public static TransactionAttributeType find( final CasualServiceMetaData entry )
    {
        Objects.requireNonNull( entry, "CasualServiceMetaData can not be null." );
        return valueFromMethod( entry.getServiceMethod() )
                    .orElseGet( ()-> valueFromClass( entry.getImplementationClass() )
                        .orElse( TransactionAttributeType.REQUIRED )
                    );
    }

    private static Optional<TransactionAttributeType> valueFromMethod(Method method )
    {
        TransactionAttribute attribute = method.getAnnotation(TransactionAttribute.class );
        return Optional.ofNullable( attribute == null ? null : attribute.value() );

    }

    private static Optional<TransactionAttributeType> valueFromClass(Class<?> c )
    {
        TransactionAttribute attribute = c.getAnnotation(TransactionAttribute.class );
        return Optional.ofNullable( attribute == null ? null : attribute.value() );
    }
}
