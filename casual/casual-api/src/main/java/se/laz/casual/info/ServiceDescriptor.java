/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.info;

import java.util.Objects;

public record ServiceDescriptor(String name, Order order)
{
    public ServiceDescriptor
    {
        Objects.requireNonNull( name, "name is null" );
        Objects.requireNonNull( order, "order is null" );
    }
}
