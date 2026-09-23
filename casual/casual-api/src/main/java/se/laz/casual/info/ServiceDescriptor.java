/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.info;

import java.util.Objects;

/**
 * Composite key of service name and service order
 * {@link Order#SEQUENTIAL} is used for inbound services
 * {@link Order#CONCURRENT} is used for outbound services
 *
 * @param name
 * @param order
 */
public record ServiceDescriptor(String name, Order order)
{
    public ServiceDescriptor
    {
        Objects.requireNonNull( name, "name is null" );
        Objects.requireNonNull( order, "order is null" );
    }
}
