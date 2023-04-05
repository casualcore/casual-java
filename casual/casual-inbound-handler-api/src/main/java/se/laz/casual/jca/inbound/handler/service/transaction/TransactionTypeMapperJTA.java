/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.transaction;

import se.laz.casual.network.messages.domain.TransactionType;

import jakarta.ejb.TransactionAttributeType;
import java.util.EnumMap;
import java.util.Objects;

import static jakarta.ejb.TransactionAttributeType.MANDATORY;
import static jakarta.ejb.TransactionAttributeType.NEVER;
import static jakarta.ejb.TransactionAttributeType.NOT_SUPPORTED;
import static jakarta.ejb.TransactionAttributeType.REQUIRED;
import static jakarta.ejb.TransactionAttributeType.REQUIRES_NEW;
import static jakarta.ejb.TransactionAttributeType.SUPPORTS;
import static se.laz.casual.network.messages.domain.TransactionType.ATOMIC;
import static se.laz.casual.network.messages.domain.TransactionType.AUTOMATIC;
import static se.laz.casual.network.messages.domain.TransactionType.JOIN;
import static se.laz.casual.network.messages.domain.TransactionType.NONE;

/**
 * Mappings for Java EE TransactionAttributeType to Casual Transaction Types:
 *
 * MANDATORY	    Join
 * NEVER			None
 * NOT_SUPPORTED	None
 * REQUIRED		    Auto
 * REQUIRES_NEW	    Atomic
 * SUPPORTS		    Join
 */
public final class TransactionTypeMapperJTA
{
    private static EnumMap<TransactionAttributeType, TransactionType> mappings = new EnumMap<>(TransactionAttributeType.class);

    static{
        mappings.put( MANDATORY,        JOIN );
        mappings.put( NEVER,            NONE );
        mappings.put( NOT_SUPPORTED,    NONE );
        mappings.put( REQUIRED,         AUTOMATIC );
        mappings.put( REQUIRES_NEW,     ATOMIC );
        mappings.put( SUPPORTS,         JOIN );
    }

    /**
     * Map the provided {@link TransactionAttributeType} to the appropriate
     * {@link TransactionType}.
     * @param type to map
     * @return mapped results.
     */
    public static TransactionType map( TransactionAttributeType type )
    {
        Objects.requireNonNull( type,"TransactionAttributeType can not be null." );
        return mappings.get( type );
    }

    private TransactionTypeMapperJTA()
    {

    }
}
